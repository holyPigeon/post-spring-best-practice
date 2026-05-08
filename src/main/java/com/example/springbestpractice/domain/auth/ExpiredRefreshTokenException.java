package com.example.springbestpractice.domain.auth;

public class ExpiredRefreshTokenException extends RuntimeException {

    public ExpiredRefreshTokenException() {
        super("만료된 리프레시 토큰입니다.");
    }
}
