package com.example.springbestpractice.domain.comment;

import com.example.springbestpractice.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("댓글 도메인")
class CommentTest {

    @Nested
    @DisplayName("댓글 생성")
    class Create {

        @Test
        @DisplayName("게시글, 내용, 작성자로 댓글을 생성한다")
        void createComment() {
            // given
            Post post = Post.create("제목", "내용", "작성자");

            // when
            Comment comment = Comment.create(post, "댓글 내용", "댓글 작성자");

            // then
            assertThat(comment)
                    .extracting("post", "content", "author")
                    .containsExactly(post, "댓글 내용", "댓글 작성자");
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class Update {

        @Test
        @DisplayName("내용을 변경하면 업데이트한다")
        void updateContent() {
            // given
            Post post = Post.create("제목", "내용", "작성자");
            Comment comment = Comment.create(post, "댓글 내용", "댓글 작성자");

            // when
            comment.updateContent("새 댓글 내용");

            // then
            assertThat(comment.getContent()).isEqualTo("새 댓글 내용");
        }
    }
}
