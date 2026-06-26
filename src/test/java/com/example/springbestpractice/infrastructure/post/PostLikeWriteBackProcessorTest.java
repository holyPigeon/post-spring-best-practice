package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.PostLike;
import com.example.springbestpractice.support.fixture.PostFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Post like write-back processor")
@ExtendWith(MockitoExtension.class)
class PostLikeWriteBackProcessorTest {

    @Mock
    PostLikeRedisRepository postLikeRedisRepository;

    @Mock
    PostLikeRepository postLikeRepository;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostLikeWriteBackProcessor processor;

    @Test
    @DisplayName("LIKE 이벤트는 post_likes에 멱등 저장하고 컬럼을 Redis 카운트로 동기화한다")
    void persistLike() {
        // given
        PostLikeChange change = new PostLikeChange(PostLikeOperation.LIKE, 1L, 2L, "1-0");
        given(postRepository.existsById(1L)).willReturn(true);
        given(postLikeRepository.existsByPostIdAndUserId(1L, 2L)).willReturn(false);
        given(postRepository.getReferenceById(1L)).willReturn(PostFixture.postWithId(1L));
        given(postLikeRedisRepository.count(1L)).willReturn(3L);

        // when
        processor.persist(List.of(change));

        // then
        verify(postLikeRepository).save(any(PostLike.class));
        verify(postRepository).updateLikeCount(1L, 3L);
    }

    @Test
    @DisplayName("이미 저장된 LIKE는 다시 저장하지 않는다(멱등)")
    void persistLikeIdempotent() {
        // given
        PostLikeChange change = new PostLikeChange(PostLikeOperation.LIKE, 1L, 2L, "1-0");
        given(postRepository.existsById(1L)).willReturn(true);
        given(postLikeRepository.existsByPostIdAndUserId(1L, 2L)).willReturn(true);
        given(postLikeRedisRepository.count(1L)).willReturn(1L);

        // when
        processor.persist(List.of(change));

        // then
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postRepository).updateLikeCount(1L, 1L);
    }

    @Test
    @DisplayName("삭제된 게시글의 LIKE는 스킵한다(FK 위반 방지)")
    void skipLikeWhenPostDeleted() {
        // given
        PostLikeChange change = new PostLikeChange(PostLikeOperation.LIKE, 1L, 2L, "1-0");
        given(postRepository.existsById(1L)).willReturn(false);
        given(postLikeRedisRepository.count(1L)).willReturn(0L);

        // when
        processor.persist(List.of(change));

        // then
        verify(postLikeRepository, never()).existsByPostIdAndUserId(anyLong(), anyLong());
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postRepository).updateLikeCount(1L, 0L);
    }

    @Test
    @DisplayName("UNLIKE 이벤트는 post_likes에서 삭제하고 컬럼을 동기화한다")
    void persistUnlike() {
        // given
        PostLikeChange change = new PostLikeChange(PostLikeOperation.UNLIKE, 1L, 2L, "1-0");
        given(postLikeRedisRepository.count(1L)).willReturn(0L);

        // when
        processor.persist(List.of(change));

        // then
        verify(postLikeRepository).deleteByPostIdAndUserId(1L, 2L);
        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postRepository).updateLikeCount(1L, 0L);
    }
}
