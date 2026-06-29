package com.example.springbestpractice.application.auth.dto;

import com.example.springbestpractice.common.model.LoginUser;

public record CurrentUserResponse(Long id, String email, String nickname, String role) {

    public static CurrentUserResponse from(LoginUser loginUser) {
        return new CurrentUserResponse(loginUser.id(), loginUser.email(), loginUser.nickname(), loginUser.role());
    }
}
