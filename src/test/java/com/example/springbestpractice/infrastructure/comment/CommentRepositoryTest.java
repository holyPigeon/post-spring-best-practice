package com.example.springbestpractice.infrastructure.comment;

import com.example.springbestpractice.domain.comment.Comment;
import com.example.springbestpractice.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("댓글 레포지토리")
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    TestEntityManager em;

    @Nested
    @DisplayName("게시글 ID로 목록 조회")
    class FindAllByPostId {

        @Test
        @DisplayName("게시글에 속한 댓글만 ID 오름차순으로 반환한다")
        void returnCommentsByPostId() {
            // given
            Post post = em.persist(Post.create("제목", "내용", 1L, "작성자"));
            Post anotherPost = em.persist(Post.create("다른 제목", "다른 내용", 1L, "작성자"));
            Comment first = em.persist(Comment.create(post, "첫 번째 댓글", 2L, "댓글 작성자"));
            Comment second = em.persist(Comment.create(post, "두 번째 댓글", 2L, "댓글 작성자"));
            em.persist(Comment.create(anotherPost, "다른 게시글 댓글", 2L, "댓글 작성자"));
            em.flush();
            em.clear();

            // when
            List<Comment> result = commentRepository.findAllByPostIdOrderByIdAsc(post.getId());

            // then
            assertThat(result).hasSize(2)
                    .extracting("id")
                    .containsExactly(first.getId(), second.getId());
        }
    }

    @Nested
    @DisplayName("댓글 ID와 게시글 ID로 조회")
    class FindByIdAndPostId {

        @Test
        @DisplayName("댓글이 게시글에 속하면 댓글을 반환한다")
        void returnCommentWhenBelongsToPost() {
            // given
            Post post = em.persist(Post.create("제목", "내용", 1L, "작성자"));
            Comment comment = em.persist(Comment.create(post, "댓글 내용", 2L, "댓글 작성자"));
            em.flush();
            em.clear();

            // when
            Optional<Comment> result = commentRepository.findByIdAndPostId(comment.getId(), post.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo("댓글 내용");
        }

        @Test
        @DisplayName("댓글이 게시글에 속하지 않으면 빈 값을 반환한다")
        void returnEmptyWhenCommentDoesNotBelongToPost() {
            // given
            Post post = em.persist(Post.create("제목", "내용", 1L, "작성자"));
            Post anotherPost = em.persist(Post.create("다른 제목", "다른 내용", 1L, "작성자"));
            Comment comment = em.persist(Comment.create(anotherPost, "댓글 내용", 2L, "댓글 작성자"));
            em.flush();
            em.clear();

            // when
            Optional<Comment> result = commentRepository.findByIdAndPostId(comment.getId(), post.getId());

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("게시글 ID로 삭제")
    class DeleteByPostId {

        @Test
        @DisplayName("게시글에 속한 댓글만 모두 삭제한다")
        void deleteCommentsByPostId() {
            // given
            Post post = em.persist(Post.create("제목", "내용", 1L, "작성자"));
            Post anotherPost = em.persist(Post.create("다른 제목", "다른 내용", 1L, "작성자"));
            Comment first = em.persist(Comment.create(post, "첫 번째 댓글", 2L, "댓글 작성자"));
            Comment second = em.persist(Comment.create(post, "두 번째 댓글", 2L, "댓글 작성자"));
            Comment another = em.persist(Comment.create(anotherPost, "다른 게시글 댓글", 2L, "댓글 작성자"));
            em.flush();
            em.clear();

            // when
            commentRepository.deleteByPostId(post.getId());
            em.flush();
            em.clear();

            // then
            assertThat(commentRepository.findById(first.getId())).isEmpty();
            assertThat(commentRepository.findById(second.getId())).isEmpty();
            assertThat(commentRepository.findById(another.getId())).isPresent();
        }
    }
}
