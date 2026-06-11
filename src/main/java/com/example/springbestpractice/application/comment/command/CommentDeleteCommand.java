package com.example.springbestpractice.application.comment.command;

import com.example.springbestpractice.common.model.LoginUser;

public record CommentDeleteCommand(Long postId, Long commentId, LoginUser loginUser) {

    public static CommentDeleteCommand from(Long postId, Long commentId, LoginUser loginUser) {
        return new CommentDeleteCommand(postId, commentId, loginUser);
    }
}
