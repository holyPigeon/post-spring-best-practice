package com.example.springbestpractice.common.model;

import com.example.springbestpractice.domain.user.User;

public record LoginUser(Long id, String email, String nickname) {

    public static LoginUser from(User user) {
        return new LoginUser(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
    }
}
