package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record UserUpdateCommand(
        Long id,
        String nickname,
        LoginUser loginUser
) {
    public static UserUpdateCommand from(Long id, UserUpdateRequest request, LoginUser loginUser) {
        return new UserUpdateCommand(id, request.nickname(), loginUser);
    }
}
