package com.example.springbestpractice.domain.user;

import com.example.springbestpractice.common.exception.ConflictException;

public class DuplicateEmailException extends ConflictException {

    public DuplicateEmailException(String email) {
        super("이미 사용 중인 이메일입니다. email=" + email);
    }
}
