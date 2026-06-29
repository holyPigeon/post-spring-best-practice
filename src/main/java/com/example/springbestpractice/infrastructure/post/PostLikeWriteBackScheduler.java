package com.example.springbestpractice.infrastructure.post;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * write-behind 워커. 주기적으로 Redis 변경 이벤트를 배치 소비해 DB에 영속화한 뒤 ack 한다.
 * ack는 영속화 트랜잭션 커밋 이후에만 수행해, 커밋 실패 시 이벤트가 보존되어 재처리(at-least-once)되도록 한다.
 */
@Component
public class PostLikeWriteBackScheduler {

    private final PostLikeRedisRepository postLikeRedisRepository;
    private final PostLikeWriteBackProcessor processor;
    private final int batchSize;

    public PostLikeWriteBackScheduler(PostLikeRedisRepository postLikeRedisRepository,
                                      PostLikeWriteBackProcessor processor,
                                      @Value("${app.post-like.flush-batch-size}") int batchSize) {
        this.postLikeRedisRepository = postLikeRedisRepository;
        this.processor = processor;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.post-like.flush-interval-ms}")
    public void flush() {
        List<PostLikeChange> changes = postLikeRedisRepository.poll(batchSize);
        if (changes.isEmpty()) {
            return;
        }
        processor.persist(changes);
        postLikeRedisRepository.ack(changes);
    }
}
