package com.example.springbestpractice.infrastructure.post;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Post like redis repository (Testcontainers)")
class PostLikeRedisRepositoryTest {

    private static GenericContainer<?> redis;
    private static LettuceConnectionFactory connectionFactory;
    private StringRedisTemplate redisTemplate;
    private PostLikeRedisRepository repository;

    @BeforeAll
    static void startContainer() {
        Assumptions.assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker is not available");
        redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
        redis.start();
        connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
    }

    @AfterAll
    static void stopContainer() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
        if (redis != null) {
            redis.stop();
        }
    }

    @BeforeEach
    void setUp() {
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        repository = new PostLikeRedisRepository(redisTemplate, "test-group", 1000L, 3600L);
    }

    @Test
    @DisplayName("like는 멤버십에 추가하고 카운트를 올리며 중복은 멱등 처리한다")
    void likeAddsMemberAndCounts() {
        assertThat(repository.like(1L, 2L)).isEqualTo(1L);
        assertThat(repository.like(1L, 2L)).isEqualTo(1L); // dedupe
        assertThat(repository.like(1L, 3L)).isEqualTo(2L);

        assertThat(repository.count(1L)).isEqualTo(2L);
        assertThat(repository.contains(1L, 2L)).isTrue();
        assertThat(repository.contains(1L, 99L)).isFalse();
    }

    @Test
    @DisplayName("unlike는 멤버십에서 제거하고 카운트를 내린다")
    void unlikeRemovesMember() {
        repository.like(1L, 2L);
        repository.like(1L, 3L);

        assertThat(repository.unlike(1L, 2L)).isEqualTo(1L);
        assertThat(repository.contains(1L, 2L)).isFalse();
        assertThat(repository.unlike(1L, 2L)).isEqualTo(1L); // 이미 없으면 멱등
    }

    @Test
    @DisplayName("ensureLoaded는 미적재일 때만 DB 라이커를 한 번 적재한다")
    void ensureLoadedLoadsOnce() {
        AtomicInteger loaderCalls = new AtomicInteger();

        repository.ensureLoaded(5L, () -> {
            loaderCalls.incrementAndGet();
            return List.of(10L, 11L);
        });
        repository.ensureLoaded(5L, () -> {
            loaderCalls.incrementAndGet();
            return List.of(10L, 11L);
        });

        assertThat(loaderCalls.get()).isEqualTo(1);
        assertThat(repository.count(5L)).isEqualTo(2L);
        assertThat(repository.contains(5L, 10L)).isTrue();
    }

    @Test
    @DisplayName("실제 변경만 이벤트로 남고 poll/ack 이후에는 비워진다")
    void pollReturnsChangesAndAckClears() {
        repository.like(1L, 2L);
        repository.like(1L, 2L); // dedupe: 이벤트 없음
        repository.like(1L, 3L);
        repository.unlike(1L, 2L);

        List<PostLikeChange> changes = repository.poll(100);

        assertThat(changes)
                .extracting(PostLikeChange::operation, PostLikeChange::postId, PostLikeChange::userId)
                .containsExactly(
                        tuple(PostLikeOperation.LIKE, 1L, 2L),
                        tuple(PostLikeOperation.LIKE, 1L, 3L),
                        tuple(PostLikeOperation.UNLIKE, 1L, 2L));

        repository.ack(changes);
        assertThat(repository.poll(100)).isEmpty();
    }

    @Test
    @DisplayName("ack 전에 죽으면 다음 poll이 미ack 엔트리를 재처리한다(크래시 복구)")
    void pollReprocessesUnackedOnRecovery() {
        repository.like(1L, 2L);

        List<PostLikeChange> delivered = repository.poll(100); // ack 없이 '크래시'
        assertThat(delivered).hasSize(1);

        List<PostLikeChange> recovered = repository.poll(100); // PEL 재처리
        assertThat(recovered)
                .extracting(PostLikeChange::recordId)
                .isEqualTo(delivered.stream().map(PostLikeChange::recordId).toList());

        repository.ack(recovered);
        assertThat(repository.poll(100)).isEmpty();
    }

    @Test
    @DisplayName("쓰기 시 멤버십/플래그에 TTL이 걸려 idle 글이 회수될 수 있다")
    void writeSetsTtl() {
        repository.ensureLoaded(1L, List::of);
        repository.like(1L, 2L);

        assertThat(redisTemplate.getExpire("post:like:users:1")).isPositive();
        assertThat(redisTemplate.getExpire("post:like:loaded:1")).isPositive();
    }

    @Test
    @DisplayName("evict는 멤버십/적재 플래그를 제거한다")
    void evictClearsMembership() {
        repository.ensureLoaded(7L, () -> List.of(1L, 2L));
        repository.like(7L, 3L);

        repository.evict(7L);

        assertThat(repository.count(7L)).isZero();
        assertThat(repository.contains(7L, 1L)).isFalse();
    }
}
