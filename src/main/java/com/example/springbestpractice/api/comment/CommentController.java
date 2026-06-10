package com.example.springbestpractice.api.comment;

import com.example.springbestpractice.application.comment.CommentService;
import com.example.springbestpractice.application.comment.command.CommentCreateCommand;
import com.example.springbestpractice.application.comment.command.CommentDeleteCommand;
import com.example.springbestpractice.application.comment.command.CommentUpdateCommand;
import com.example.springbestpractice.application.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.application.comment.dto.CommentResponse;
import com.example.springbestpractice.application.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.model.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(CommentCreateCommand.from(postId, request, loginUser)));
    }

    @GetMapping
    public List<CommentResponse> getComments(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }

    @GetMapping("/{commentId}")
    public CommentResponse getComment(@PathVariable Long postId, @PathVariable Long commentId) {
        return commentService.getComment(postId, commentId);
    }

    @PutMapping("/{commentId}")
    public CommentResponse updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        return commentService.updateComment(CommentUpdateCommand.from(postId, commentId, request, loginUser));
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @CurrentUser LoginUser loginUser
    ) {
        commentService.deleteComment(CommentDeleteCommand.from(postId, commentId, loginUser));
    }
}
