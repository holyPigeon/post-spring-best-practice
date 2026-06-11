package com.example.springbestpractice.domain.auth;

import com.example.springbestpractice.common.exception.UnauthorizedException;

public class RefreshTokenNotFoundException extends UnauthorizedException {

    public RefreshTokenNotFoundException() {
        super("유효하지 않은 리프레시 토큰입니다.");
    }
}
