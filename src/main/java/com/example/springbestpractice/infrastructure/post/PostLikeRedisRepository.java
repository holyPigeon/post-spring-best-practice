package com.example.springbestpractice.infrastructure.post;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 좋아요의 Redis online store. 멤버십 Set / 카운트 / 변경 이벤트 Stream 등 좋아요 관련 Redis 키를 모두 소유한다.
 * RDB의 system of record({@code PostLikeRepository})와 구분되는 별도 계층이다.
 *
 * <p>멤버십 변경(SADD/SREM)과 변경 이벤트 적재(XADD)는 Lua로 원자 처리해, 상태만 바뀌고 영속 의도가 유실되는 일을 막는다.
 */
@Repository
public class PostLikeRedisRepository {

    private static final String MEMBERS_KEY_PREFIX = "post:like:users:";
    private static final String LOADED_KEY_PREFIX = "post:like:loaded:";
    private static final String CHANGES_STREAM_KEY = "post:like:changes";
    private static final String CONSUMER_NAME = "writeback";

    private static final RedisScript<Long> LIKE_SCRIPT = new DefaultRedisScript<>("""
            local added = redis.call('SADD', KEYS[1], ARGV[1])
            if added == 1 then
              redis.call('XADD', KEYS[2], '*', 'op', 'LIKE', 'postId', ARGV[2], 'userId', ARGV[1])
            end
            return redis.call('SCARD', KEYS[1])
            """, Long.class);

    private static final RedisScript<Long> UNLIKE_SCRIPT = new DefaultRedisScript<>("""
            local removed = redis.call('SREM', KEYS[1], ARGV[1])
            if removed == 1 then
              redis.call('XADD', KEYS[2], '*', 'op', 'UNLIKE', 'postId', ARGV[2], 'userId', ARGV[1])
            end
            return redis.call('SCARD', KEYS[1])
            """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final String consumerGroup;

    public PostLikeRedisRepository(StringRedisTemplate redisTemplate,
                                   @Value("${app.post-like.consumer-group}") String consumerGroup) {
        this.redisTemplate = redisTemplate;
        this.consumerGroup = consumerGroup;
    }

    /** 좋아요를 멤버십에 추가하고(신규일 때만 변경 이벤트 적재) 현재 카운트를 반환한다. */
    public long like(Long postId, Long userId) {
        return runChange(LIKE_SCRIPT, postId, userId);
    }

    /** 좋아요를 멤버십에서 제거하고(제거됐을 때만 변경 이벤트 적재) 현재 카운트를 반환한다. */
    public long unlike(Long postId, Long userId) {
        return runChange(UNLIKE_SCRIPT, postId, userId);
    }

    public long count(Long postId) {
        Long size = redisTemplate.opsForSet().size(membersKey(postId));
        return size == null ? 0L : size;
    }

    public boolean contains(Long postId, Long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(membersKey(postId), String.valueOf(userId)));
    }

    /** 멤버십 Set이 아직 적재되지 않았으면 DB의 라이커를 적재한다. (적재 여부는 별도 플래그 키로 구분) */
    public void ensureLoaded(Long postId, Supplier<List<Long>> dbLoader) {
        String loadedKey = loadedKey(postId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(loadedKey))) {
            return;
        }
        List<Long> userIds = dbLoader.get();
        if (!userIds.isEmpty()) {
            String[] members = userIds.stream().map(String::valueOf).toArray(String[]::new);
            redisTemplate.opsForSet().add(membersKey(postId), members);
        }
        redisTemplate.opsForValue().set(loadedKey, "1");
    }

    /** 게시글 삭제 시 멤버십/플래그 키를 제거한다. (이후 좋아요는 게시글 존재 검증에서 차단되므로 부활 위험 없음) */
    public void evict(Long postId) {
        redisTemplate.delete(List.of(membersKey(postId), loadedKey(postId)));
    }

    /** 변경 이벤트를 배치로 소비한다. 새 스트림/그룹은 필요 시 생성한다. */
    public List<PostLikeChange> poll(int batchSize) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(CHANGES_STREAM_KEY))) {
            return List.of();
        }
        ensureGroup();
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();
        List<MapRecord<String, Object, Object>> records = streamOps.read(
                Consumer.from(consumerGroup, CONSUMER_NAME),
                StreamReadOptions.empty().count(batchSize),
                StreamOffset.create(CHANGES_STREAM_KEY, ReadOffset.lastConsumed()));
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        List<PostLikeChange> changes = new ArrayList<>(records.size());
        for (MapRecord<String, Object, Object> record : records) {
            Map<Object, Object> value = record.getValue();
            changes.add(new PostLikeChange(
                    PostLikeOperation.valueOf((String) value.get("op")),
                    Long.valueOf((String) value.get("postId")),
                    Long.valueOf((String) value.get("userId")),
                    record.getId().getValue()));
        }
        return changes;
    }

    /** 처리 완료된 이벤트를 ack 하고 스트림에서 제거한다. */
    public void ack(List<PostLikeChange> changes) {
        if (changes.isEmpty()) {
            return;
        }
        RecordId[] ids = changes.stream()
                .map(change -> RecordId.of(change.recordId()))
                .toArray(RecordId[]::new);
        redisTemplate.opsForStream().acknowledge(CHANGES_STREAM_KEY, consumerGroup, ids);
        redisTemplate.opsForStream().delete(CHANGES_STREAM_KEY, ids);
    }

    private long runChange(RedisScript<Long> script, Long postId, Long userId) {
        Long count = redisTemplate.execute(
                script,
                List.of(membersKey(postId), CHANGES_STREAM_KEY),
                String.valueOf(userId), String.valueOf(postId));
        return count == null ? 0L : count;
    }

    private void ensureGroup() {
        try {
            redisTemplate.opsForStream().createGroup(CHANGES_STREAM_KEY, ReadOffset.from("0"), consumerGroup);
        } catch (DataAccessException alreadyExists) {
            // BUSYGROUP: consumer group already created.
        }
    }

    private String membersKey(Long postId) {
        return MEMBERS_KEY_PREFIX + postId;
    }

    private String loadedKey(Long postId) {
        return LOADED_KEY_PREFIX + postId;
    }
}
