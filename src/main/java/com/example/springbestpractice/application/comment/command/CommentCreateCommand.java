package com.example.springbestpractice.application.comment.command;

import com.example.springbestpractice.application.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record CommentCreateCommand(
        Long postId,
        String content,
        LoginUser loginUser
) {
    public static CommentCreateCommand from(Long postId, CommentCreateRequest request, LoginUser loginUser) {
        return new CommentCreateCommand(postId, request.content(), loginUser);
    }
}
