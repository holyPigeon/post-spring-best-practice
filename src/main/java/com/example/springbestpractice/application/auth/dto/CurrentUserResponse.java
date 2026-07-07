package com.example.springbestpractice.application.auth.dto;

import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.common.model.Role;

public record CurrentUserResponse(Long id, String email, String nickname, Role role) {

    public static CurrentUserResponse from(LoginUser loginUser) {
        return new CurrentUserResponse(
                loginUser.id(), loginUser.email(), loginUser.nickname(), loginUser.role());
    }
}
