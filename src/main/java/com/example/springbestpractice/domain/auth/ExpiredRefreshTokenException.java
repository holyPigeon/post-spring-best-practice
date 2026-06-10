package com.example.springbestpractice.domain.auth;

import com.example.springbestpractice.common.exception.UnauthorizedException;

public class ExpiredRefreshTokenException extends UnauthorizedException {

    public ExpiredRefreshTokenException() {
        super("만료된 리프레시 토큰입니다.");
    }
}
