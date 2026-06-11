package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;

public record UserPasswordUpdateCommand(
        Long id,
        String password,
        LoginUser loginUser
) {
    public static UserPasswordUpdateCommand from(Long id, UserPasswordUpdateRequest request, LoginUser loginUser) {
        return new UserPasswordUpdateCommand(id, request.password(), loginUser);
    }
}
