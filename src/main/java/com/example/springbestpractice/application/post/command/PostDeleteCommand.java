package com.example.springbestpractice.application.post.command;

import com.example.springbestpractice.common.model.LoginUser;

public record PostDeleteCommand(Long id, LoginUser loginUser) {

    public static PostDeleteCommand from(Long id, LoginUser loginUser) {
        return new PostDeleteCommand(id, loginUser);
    }
}
