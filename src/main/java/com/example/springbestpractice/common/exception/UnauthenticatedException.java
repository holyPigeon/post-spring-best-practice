package com.example.springbestpractice.common.exception;

public class UnauthenticatedException extends RuntimeException {

    public UnauthenticatedException() {
        super("인증 정보가 없습니다.");
    }
}
