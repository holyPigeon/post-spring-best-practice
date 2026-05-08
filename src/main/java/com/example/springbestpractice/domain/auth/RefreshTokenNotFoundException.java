package com.example.springbestpractice.domain.auth;

public class RefreshTokenNotFoundException extends RuntimeException {

    public RefreshTokenNotFoundException() {
        super("유효하지 않은 리프레시 토큰입니다.");
    }
}
