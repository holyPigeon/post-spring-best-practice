package com.example.springbestpractice.domain.post;

import com.example.springbestpractice.support.fixture.PostFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Post like domain")
class PostLikeTest {

    @Test
    @DisplayName("create post like with post and user id")
    void createPostLike() {
        // given
        Post post = PostFixture.postWithId(1L);

        // when
        PostLike postLike = PostLike.create(post, 2L);

        // then
        assertThat(postLike)
                .extracting("post", "userId")
                .containsExactly(post, 2L);
    }

    @Test
    @DisplayName("throw exception when user id is null")
    void throwExceptionWhenUserIdNull() {
        assertThatThrownBy(() -> PostLike.create(PostFixture.post(), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
