package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.common.model.LoginUser;

public record UserDeleteCommand(Long id, LoginUser loginUser) {

    public static UserDeleteCommand from(Long id, LoginUser loginUser) {
        return new UserDeleteCommand(id, loginUser);
    }
}
