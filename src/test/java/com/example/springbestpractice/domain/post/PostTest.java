package com.example.springbestpractice.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("게시글 도메인")
class PostTest {

    @Nested
    @DisplayName("게시글 생성")
    class Create {

        @Test
        @DisplayName("제목·내용·작성자로 게시글을 생성한다")
        void createPost() {
            // when
            Post post = Post.create("제목", "내용", "작성자");

            // then
            assertThat(post)
                    .extracting("title", "content", "author")
                    .containsExactly("제목", "내용", "작성자");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class Update {

        @Test
        @DisplayName("제목과 내용을 변경하면 업데이트된다")
        void updatePost() {
            // given
            Post post = Post.create("제목", "내용", "작성자");

            // when
            post.update("새제목", "새내용");

            // then
            assertThat(post)
                    .extracting("title", "content")
                    .containsExactly("새제목", "새내용");
        }
    }
}
