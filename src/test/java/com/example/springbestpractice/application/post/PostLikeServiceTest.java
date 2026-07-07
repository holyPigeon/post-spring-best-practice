package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostLike;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import com.example.springbestpractice.support.fixture.PostFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    PostLikeService postLikeService;

    private Post post;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        post = PostFixture.postWithId(1L);
        loginUser = new LoginUser(2L, "user@test.com", "user", UserRole.USER);
    }

    @Nested
    @DisplayName("like")
    class Like {

        @Test
        @DisplayName("save like and increase post like count")
        void likePost() {
            // given
            Post likedPost = PostFixture.postWithLikeCount(1L, 1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post), Optional.of(likedPost));
            given(postLikeRepository.existsByPostIdAndUserId(1L, 2L)).willReturn(false);

            // when
            PostLikeResponse result = postLikeService.like(PostLikeCreateCommand.from(1L, loginUser));

            // then
            ArgumentCaptor<PostLike> postLikeCaptor = ArgumentCaptor.forClass(PostLike.class);
            verify(postLikeRepository).save(postLikeCaptor.capture());
            assertThat(postLikeCaptor.getValue())
                    .extracting("post", "userId")
                    .containsExactly(post, 2L);
            verify(postRepository).increaseLikeCount(1L);
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 1L, true);
        }

        @Test
        @DisplayName("return current state when already liked")
        void returnCurrentStateWhenAlreadyLiked() {
            // given
            post = PostFixture.postWithLikeCount(1L, 1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(postLikeRepository.existsByPostIdAndUserId(1L, 2L)).willReturn(true);

            // when
            PostLikeResponse result = postLikeService.like(PostLikeCreateCommand.from(1L, loginUser));

            // then
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 1L, true);
            verify(postLikeRepository, never()).save(any(PostLike.class));
            verify(postRepository, never()).increaseLikeCount(1L);
        }

        @Test
        @DisplayName("throw exception when post does not exist")
        void throwExceptionWhenPostNotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postLikeService.like(PostLikeCreateCommand.from(999L, loginUser)))
                    .isInstanceOf(PostNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("unlike")
    class Unlike {

        @Test
        @DisplayName("delete like and decrease post like count")
        void unlikePost() {
            // given
            post = PostFixture.postWithLikeCount(1L, 1L);
            Post unlikedPost = PostFixture.postWithId(1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post), Optional.of(unlikedPost));
            given(postLikeRepository.deleteByPostIdAndUserId(1L, 2L)).willReturn(1);

            // when
            PostLikeResponse result = postLikeService.unlike(PostLikeDeleteCommand.from(1L, loginUser));

            // then
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 0L, false);
            verify(postRepository).decreaseLikeCount(1L);
        }

        @Test
        @DisplayName("return current state when not liked")
        void returnCurrentStateWhenNotLiked() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(postLikeRepository.deleteByPostIdAndUserId(1L, 2L)).willReturn(0);

            // when
            PostLikeResponse result = postLikeService.unlike(PostLikeDeleteCommand.from(1L, loginUser));

            // then
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 0L, false);
            verify(postRepository, never()).decreaseLikeCount(1L);
            verify(postLikeRepository, never()).delete(any(PostLike.class));
        }
    }
}
