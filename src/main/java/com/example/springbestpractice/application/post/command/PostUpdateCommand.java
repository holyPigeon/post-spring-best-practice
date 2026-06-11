package com.example.springbestpractice.application.post.command;

import com.example.springbestpractice.application.post.dto.PostUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record PostUpdateCommand(
        Long id,
        String title,
        String content,
        LoginUser loginUser
) {
    public static PostUpdateCommand from(Long id, PostUpdateRequest request, LoginUser loginUser) {
        return new PostUpdateCommand(id, request.title(), request.content(), loginUser);
    }
}
