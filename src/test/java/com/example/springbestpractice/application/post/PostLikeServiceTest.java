package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import com.example.springbestpractice.support.fixture.PostFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@DisplayName("Post like service")
@ExtendWith(MockitoExtension.class)
class PostLikeServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    PostLikeRepository postLikeRepository;

    @Mock
    PostLikeWriter postLikeWriter;

    @InjectMocks
    PostLikeService postLikeService;

    private Post post;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        post = PostFixture.postWithId(1L);
        loginUser = new LoginUser(2L, "user@test.com", "user");
    }

    @Nested
    @DisplayName("like")
    class Like {

        @Test
        @DisplayName("insert like and sync post like count")
        void likePost() {
            // given
            Post likedPost = PostFixture.postWithLikeCount(1L, 1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post), Optional.of(likedPost));

            // when
            PostLikeResponse result = postLikeService.like(PostLikeCreateCommand.from(1L, loginUser));

            // then
            verify(postLikeWriter).insert(1L, 2L);
            verify(postRepository).syncLikeCount(1L);
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 1L, true);
        }

        @Test
        @DisplayName("stay idempotent and still sync count when already liked")
        void likeIdempotent() {
            // given
            Post likedPost = PostFixture.postWithLikeCount(1L, 1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post), Optional.of(likedPost));
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(postLikeWriter).insert(1L, 2L);

            // when
            PostLikeResponse result = postLikeService.like(PostLikeCreateCommand.from(1L, loginUser));

            // then
            verify(postRepository).syncLikeCount(1L);
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 1L, true);
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
        @DisplayName("delete like and sync post like count")
        void unlikePost() {
            // given
            post = PostFixture.postWithLikeCount(1L, 1L);
            Post unlikedPost = PostFixture.postWithId(1L);
            given(postRepository.findById(1L)).willReturn(Optional.of(post), Optional.of(unlikedPost));

            // when
            PostLikeResponse result = postLikeService.unlike(PostLikeDeleteCommand.from(1L, loginUser));

            // then
            verify(postLikeRepository).deleteByPostIdAndUserId(1L, 2L);
            verify(postRepository).syncLikeCount(1L);
            assertThat(result)
                    .extracting("postId", "likeCount", "liked")
                    .containsExactly(1L, 0L, false);
        }
    }
}
