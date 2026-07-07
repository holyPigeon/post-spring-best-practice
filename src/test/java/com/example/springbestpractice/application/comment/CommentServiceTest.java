package com.example.springbestpractice.application.comment;

import com.example.springbestpractice.application.comment.command.CommentCreateCommand;
import com.example.springbestpractice.application.comment.command.CommentDeleteCommand;
import com.example.springbestpractice.application.comment.command.CommentUpdateCommand;
import com.example.springbestpractice.application.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.application.comment.dto.CommentResponse;
import com.example.springbestpractice.application.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.domain.comment.Comment;
import com.example.springbestpractice.domain.comment.CommentNotFoundException;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import com.example.springbestpractice.support.fixture.CommentFixture;
import com.example.springbestpractice.support.fixture.PostFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        post = PostFixture.postWithId(1L);
        comment = CommentFixture.commentWithId(1L, post);
        loginUser = new LoginUser(2L, "commenter@test.com", "댓글 작성자", UserRole.USER);
    }

    @Nested
    @DisplayName("댓글 생성")
    class Create {

        @Test
        @DisplayName("존재하지 않는 게시글 ID면 예외를 던진다")
        void throwExceptionWhenPostNotFound() {
            // given
            CommentCreateRequest request = new CommentCreateRequest("comment");
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.createComment(CommentCreateCommand.from(999L, request, loginUser)))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("정상 입력이면 댓글을 저장하고 응답을 반환한다")
        void createComment() {
            // given
            CommentCreateRequest request = new CommentCreateRequest("comment");
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(commentRepository.save(any(Comment.class))).willReturn(comment);

            // when
            CommentResponse result = commentService.createComment(CommentCreateCommand.from(1L, request, loginUser));

            // then
            assertThat(result)
                    .extracting("id", "postId", "content")
                    .containsExactly(1L, 1L, "comment");
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Test
    @DisplayName("게시글에 댓글이 있으면 댓글 목록을 반환한다")
    void returnComments() {
        // given
        Comment another = CommentFixture.commentWithId(2L, post, "두 번째 댓글", 2L, "댓글 작성자");
        given(postRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findAllByPostIdOrderByIdAsc(1L)).willReturn(List.of(comment, another));

        // when
        List<CommentResponse> result = commentService.getComments(1L);

        // then
        assertThat(result).hasSize(2)
                .extracting("content")
                .containsExactly("comment", "두 번째 댓글");
    }

    @Nested
    @DisplayName("댓글 단건 조회")
    class Find {

        @Test
        @DisplayName("존재하지 않는 댓글 ID면 예외를 던진다")
        void throwExceptionWhenCommentNotFound() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.getComment(1L, 999L))
                    .isInstanceOf(CommentNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 응답을 반환한다")
        void returnCommentResponse() {
            // given
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            CommentResponse result = commentService.getComment(1L, 1L);

            // then
            assertThat(result.content()).isEqualTo("comment");
        }
    }

    @Nested
    @DisplayName("댓글 수정")
    class Update {

        @Test
        @DisplayName("작성자면 내용을 수정한다")
        void updateComment() {
            // given
            CommentUpdateRequest request = new CommentUpdateRequest("새 댓글 내용");
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

            // when
            CommentResponse result = commentService.updateComment(CommentUpdateCommand.from(1L, 1L, request, loginUser));

            // then
            assertThat(result.content()).isEqualTo("새 댓글 내용");
        }

        @Test
        @DisplayName("작성자가 아니면 예외를 던진다")
        void throwExceptionWhenNotOwner() {
            // given
            CommentUpdateRequest request = new CommentUpdateRequest("새 댓글 내용");
            LoginUser otherUser = new LoginUser(1L, "writer@test.com", "작성자", UserRole.USER);
            given(postRepository.existsById(1L)).willReturn(true);
            given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() -> commentService.updateComment(CommentUpdateCommand.from(1L, 1L, request, otherUser)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    @DisplayName("작성자면 댓글을 삭제한다")
    void deleteComment() {
        // given
        given(postRepository.existsById(1L)).willReturn(true);
        given(commentRepository.findByIdAndPostId(1L, 1L)).willReturn(Optional.of(comment));

        // when
        commentService.deleteComment(CommentDeleteCommand.from(1L, 1L, loginUser));

        // then
        verify(commentRepository).delete(comment);
    }
}
