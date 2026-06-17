package com.example.springbestpractice.domain.comment;

import com.example.springbestpractice.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("댓글 도메인")
class CommentTest {

    @Nested
    @DisplayName("댓글 생성")
    class Create {

        @Test
        @DisplayName("게시글, 내용, 작성자로 댓글을 생성한다")
        void createComment() {
            // given
            Post post = Post.create("제목", "내용", 1L, "작성자");

            // when
            Comment comment = Comment.create(post, "댓글 내용", 2L, "댓글 작성자");

            // then
            assertThat(comment)
                    .extracting("post", "content", "authorId", "authorNickname")
                    .containsExactly(post, "댓글 내용", 2L, "댓글 작성자");
        }

        @Test
        @DisplayName("작성자 아이디가 없으면 예외를 던진다")
        void throwExceptionWhenAuthorIdNull() {
            // given
            Post post = Post.create("제목", "내용", 1L, "작성자");

            // when & then
            assertThatThrownBy(() -> Comment.create(post, "댓글 내용", null, "댓글 작성자"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("작성자 아이디는 필수입니다.");
        }
    }

    @Nested
    @DisplayName("댓글 소유자 확인")
    class Ownership {

        @Test
        @DisplayName("작성자 아이디가 같으면 true를 반환한다")
        void returnTrueWhenOwner() {
            // given
            Post post = Post.create("제목", "내용", 1L, "작성자");
            Comment comment = Comment.create(post, "댓글 내용", 2L, "댓글 작성자");

            // when & then
            assertThat(comment.isWrittenBy(2L)).isTrue();
        }

        @Test
        @DisplayName("작성자 아이디가 다르면 false를 반환한다")
        void returnFalseWhenNotOwner() {
            // given
            Post post = Post.create("제목", "내용", 1L, "작성자");
            Comment comment = Comment.create(post, "댓글 내용", 2L, "댓글 작성자");

            // when & then
            assertThat(comment.isWrittenBy(1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class Update {

        @Test
        @DisplayName("내용을 변경하면 업데이트한다")
        void updateContent() {
            // given
            Post post = Post.create("제목", "내용", 1L, "작성자");
            Comment comment = Comment.create(post, "댓글 내용", 2L, "댓글 작성자");

            // when
            comment.updateContent("새 댓글 내용");

            // then
            assertThat(comment.getContent()).isEqualTo("새 댓글 내용");
        }

        @Test
        @DisplayName("내용이 비어 있으면 예외를 던진다")
        void throwExceptionWhenContentBlank() {
            // given
            Post post = Post.create("제목", "내용", 1L, "작성자");
            Comment comment = Comment.create(post, "댓글 내용", 2L, "댓글 작성자");

            // when & then
            assertThatThrownBy(() -> comment.updateContent(" "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("댓글 내용은 필수입니다.");
        }
    }
}
