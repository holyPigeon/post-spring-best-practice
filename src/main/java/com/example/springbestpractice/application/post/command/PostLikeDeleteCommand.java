package com.example.springbestpractice.application.post.command;

import com.example.springbestpractice.common.model.LoginUser;

public record PostLikeDeleteCommand(
        Long postId,
        LoginUser loginUser
) {
    public static PostLikeDeleteCommand from(Long postId, LoginUser loginUser) {
        return new PostLikeDeleteCommand(postId, loginUser);
    }
}
