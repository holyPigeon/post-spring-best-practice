package com.example.springbestpractice.application.post.command;

import com.example.springbestpractice.common.model.LoginUser;

public record PostLikeCreateCommand(
        Long postId,
        LoginUser loginUser
) {
    public static PostLikeCreateCommand from(Long postId, LoginUser loginUser) {
        return new PostLikeCreateCommand(postId, loginUser);
    }
}
