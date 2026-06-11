package com.example.springbestpractice.domain.user;

import com.example.springbestpractice.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(Long id) {
        super("유저를 찾을 수 없습니다. id=" + id);
    }
}
