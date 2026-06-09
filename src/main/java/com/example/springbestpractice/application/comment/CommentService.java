package com.example.springbestpractice.application.comment;

import com.example.springbestpractice.application.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.application.comment.dto.CommentResponse;
import com.example.springbestpractice.application.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.comment.Comment;
import com.example.springbestpractice.domain.comment.CommentNotFoundException;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request, LoginUser loginUser) {
        LoginUser author = requireLoginUser(loginUser);
        Post post = getPost(postId);
        Comment comment = Comment.create(post, request.content(), author.id(), author.nickname());
        return CommentResponse.from(commentRepository.save(comment));
    }

    public List<CommentResponse> getComments(Long postId) {
        validatePostExists(postId);
        return commentRepository.findAllByPostIdOrderByIdAsc(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse getComment(Long postId, Long commentId) {
        validatePostExists(postId);
        return CommentResponse.from(getCommentByPostId(postId, commentId));
    }

    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentUpdateRequest request, LoginUser loginUser) {
        validatePostExists(postId);
        Comment comment = getCommentByPostId(postId, commentId);
        validateOwner(comment, loginUser);
        comment.updateContent(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, LoginUser loginUser) {
        validatePostExists(postId);
        Comment comment = getCommentByPostId(postId, commentId);
        validateOwner(comment, loginUser);
        commentRepository.delete(comment);
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }
    }

    private Comment getCommentByPostId(Long postId, Long commentId) {
        return commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
    }

    private void validateOwner(Comment comment, LoginUser loginUser) {
        LoginUser owner = requireLoginUser(loginUser);
        if (!comment.isWrittenBy(owner.id())) {
            throw new AccessDeniedException("댓글 소유자가 아닙니다.");
        }
    }

    private LoginUser requireLoginUser(LoginUser loginUser) {
        if (loginUser == null) {
            throw new AccessDeniedException("인증된 사용자 정보가 필요합니다.");
        }
        return loginUser;
    }
}
