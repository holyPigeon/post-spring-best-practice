package com.example.springbestpractice.infrastructure.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Post like write-back scheduler")
@ExtendWith(MockitoExtension.class)
class PostLikeWriteBackSchedulerTest {

    @Mock
    PostLikeRedisRepository postLikeRedisRepository;

    @Mock
    PostLikeWriteBackProcessor processor;

    @Test
    @DisplayName("이벤트가 있으면 영속화 후 ack 한다(순서 보장)")
    void flushPersistsThenAcks() {
        // given
        PostLikeWriteBackScheduler scheduler = new PostLikeWriteBackScheduler(postLikeRedisRepository, processor, 100);
        List<PostLikeChange> changes = List.of(new PostLikeChange(PostLikeOperation.LIKE, 1L, 2L, "1-0"));
        given(postLikeRedisRepository.poll(100)).willReturn(changes);

        // when
        scheduler.flush();

        // then
        var ordered = inOrder(processor, postLikeRedisRepository);
        ordered.verify(processor).persist(changes);
        ordered.verify(postLikeRedisRepository).ack(changes);
    }

    @Test
    @DisplayName("이벤트가 없으면 영속화/ack 하지 않는다")
    void flushSkipsWhenEmpty() {
        // given
        PostLikeWriteBackScheduler scheduler = new PostLikeWriteBackScheduler(postLikeRedisRepository, processor, 100);
        given(postLikeRedisRepository.poll(100)).willReturn(List.of());

        // when
        scheduler.flush();

        // then
        verify(processor, never()).persist(anyList());
        verify(postLikeRedisRepository, never()).ack(anyList());
    }
}
