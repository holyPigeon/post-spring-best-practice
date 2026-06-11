package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record UserPasswordUpdateCommand(
        String password,
        LoginUser loginUser
) {
    public static UserPasswordUpdateCommand from(UserPasswordUpdateRequest request, LoginUser loginUser) {
        return new UserPasswordUpdateCommand(request.password(), loginUser);
    }
}
