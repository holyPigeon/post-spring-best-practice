package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.common.model.LoginUser;

public record UserDeleteCommand(LoginUser loginUser) {

    public static UserDeleteCommand from(LoginUser loginUser) {
        return new UserDeleteCommand(loginUser);
    }
}
