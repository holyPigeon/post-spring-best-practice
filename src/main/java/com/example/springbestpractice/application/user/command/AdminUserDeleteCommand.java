package com.example.springbestpractice.application.user.command;

import com.example.springbestpractice.common.model.LoginUser;

public record AdminUserDeleteCommand(Long targetUserId, LoginUser loginUser) {

    public static AdminUserDeleteCommand from(Long targetUserId, LoginUser loginUser) {
        return new AdminUserDeleteCommand(targetUserId, loginUser);
    }
}
