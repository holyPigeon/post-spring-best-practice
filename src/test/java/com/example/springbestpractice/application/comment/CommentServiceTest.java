package com.example.springbestpractice.application.comment;

import com.example.springbestpractice.api.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.api.comment.dto.CommentResponse;
import com.example.springbestpractice.api.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.domain.comment.Comment;
import com.example.springbestpractice.domain.comment.CommentNotFoundException;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("댓글 서비스")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    CommentService commentService;

    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .author("작성자")
                .build();
        comment = Comment.builder()
                .id(1L)
                .post(post)
                .content("댓글 내용")
                .author("댓글 작성자")
                .build();
    }

    @Nested
    @DisplayName("댓글 생성")
    class Create {

        @Test
        @DisplayName("존재하지 않는 게시글 ID면 PostNotFoundException을 던진다")
        void throwExceptionWhenPostNotFound() {
            // given
            CommentCreateRequest request = new CommentCreateRequest("댓글 내용", "댓글 작성자");
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(999L, request))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("정상 입력이면 댓글을 저장하고 CommentResponse를 반환한다")
        void createComment() {
            // given
            CommentCreateRequest request = new CommentCreateRequest("댓글 내용", "댓글 작성자");
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            // when
            CommentResponse result = commentService.createComment(1L, request);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.postId()).isEqualTo(1L);
            assertThat(result.content()).isEqualTo("댓글 내용");
            assertThat(result.author()).isEqualTo("댓글 작성자");
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회")
    class FindAll {

        @Test
        @DisplayName("존재하지 않는 게시글 ID면 PostNotFoundException을 던진다")
        void throwExceptionWhenPostNotFound() {
            // given
            given(postRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> commentService.getComments(999L))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("게시글에 댓글이 있으면 댓글 목록을 반환한다")
        void returnComments() {
            // given
            Comment another = Comment.builder()
                    .id(2L)
                    .post(post)
                    .content("두 번째 댓글")
                    .author("댓글 작성자")
                    .build();
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findAllByPostIdOrderByIdAsc(1L)).willReturn(List.of(comment, another));

            // when
            List<CommentResponse> result = commentService.getComments(1L);

            // then
            assertThat(result).hasSize(2)
                    .extracting("content")
                    .containsExactly("댓글 내용", "두 번째 댓글");
        }
    }

    @Nested
    @DisplayName("댓글 단건 조회")
    class Find {

        @Test
        @DisplayName("존재하지 않는 댓글 ID면 CommentNotFoundException을 던진다")
        void throwExceptionWhenCommentNotFound() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.getComment(1L, 999L))
                    .isInstanceOf(CommentNotFoundException.class)
                    .hasMessage("댓글을 찾을 수 없습니다. id=999");
        }

        @Test
        @DisplayName("존재하는 ID면 CommentResponse를 반환한다")
        void returnCommentResponse() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            CommentResponse result = commentService.getComment(1L, 1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.content()).isEqualTo("댓글 내용");
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class Update {

        @Test
        @DisplayName("존재하는 ID면 내용을 수정하고 반환한다")
        void updateComment() {
            // given
            CommentUpdateRequest request = new CommentUpdateRequest("새 댓글 내용");
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            CommentResponse result = commentService.updateComment(1L, 1L, request);

            // then
            assertThat(result.content()).isEqualTo("새 댓글 내용");
        }
    }

    @Nested
    @DisplayName("댓글 삭제")
    class Delete {

        @Test
        @DisplayName("존재하는 ID면 댓글을 삭제한다")
        void deleteComment() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            commentService.deleteComment(1L, 1L);

            // then
            verify(commentRepository).delete(comment);
        }
    }
}
