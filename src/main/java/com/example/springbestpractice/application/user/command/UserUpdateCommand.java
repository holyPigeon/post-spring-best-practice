package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record UserUpdateCommand(
        String nickname,
        LoginUser loginUser
) {
    public static UserUpdateCommand from(UserUpdateRequest request, LoginUser loginUser) {
        return new UserUpdateCommand(request.nickname(), loginUser);
    }
}
