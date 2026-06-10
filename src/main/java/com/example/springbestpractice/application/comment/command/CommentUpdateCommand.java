package com.example.springbestpractice.application.comment.command;

import com.example.springbestpractice.application.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record CommentUpdateCommand(
        Long postId,
        Long commentId,
        String content,
        LoginUser loginUser
) {
    public static CommentUpdateCommand from(
            Long postId,
            Long commentId,
            CommentUpdateRequest request,
            LoginUser loginUser
    ) {
        return new CommentUpdateCommand(postId, commentId, request.content(), loginUser);
    }
}
