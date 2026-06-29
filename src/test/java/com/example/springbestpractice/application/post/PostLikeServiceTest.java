package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.post.PostLikeRedisRepository;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("Post like service")
@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    PostLikeRepository postLikeRepository;

    @Mock
    PostLikeRedisRepository postLikeRedisRepository;

    @InjectMocks
    PostLikeService postLikeService;

    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        loginUser = new LoginUser(2L, "user@test.com", "user", "USER");
    }

    @Nested
    @DisplayName("like")
    class Like {

        @Test
        @DisplayName("add like in redis and return current count")
        void likePost() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(postLikeRedisRepository.like(1L, 2L)).willReturn(5L);

            // when
            PostLikeResponse result = postLikeService.like(PostLikeCreateCommand.from(1L, loginUser));

            // then
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 5L, true);
            verify(postLikeRedisRepository).like(1L, 2L);
        }

        @Test
        @DisplayName("throw exception when post does not exist")
        void throwExceptionWhenPostNotFound() {
            // given
            given(postRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> postLikeService.like(PostLikeCreateCommand.from(999L, loginUser)))
                    .isInstanceOf(PostNotFoundException.class);
            verify(postLikeRedisRepository, never()).like(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("unlike")
    class Unlike {

        @Test
        @DisplayName("remove like in redis and return current count")
        void unlikePost() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(postLikeRedisRepository.unlike(1L, 2L)).willReturn(0L);

            // when
            PostLikeResponse result = postLikeService.unlike(PostLikeDeleteCommand.from(1L, loginUser));

            // then
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 0L, false);
            verify(postLikeRedisRepository).unlike(1L, 2L);
        }

        @Test
        @DisplayName("throw exception when post does not exist")
        void throwExceptionWhenPostNotFound() {
            // given
            given(postRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> postLikeService.unlike(PostLikeDeleteCommand.from(999L, loginUser)))
                    .isInstanceOf(PostNotFoundException.class);
            verify(postLikeRedisRepository, never()).unlike(anyLong(), anyLong());
        }
    }
}
