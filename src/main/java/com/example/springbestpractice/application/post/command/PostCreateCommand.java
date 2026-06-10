package com.example.springbestpractice.application.post.command;

import com.example.springbestpractice.application.post.dto.PostCreateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record PostCreateCommand(
        String title,
        String content,
        LoginUser loginUser
) {
    public static PostCreateCommand from(PostCreateRequest request, LoginUser loginUser) {
        return new PostCreateCommand(request.title(), request.content(), loginUser);
    }
}
