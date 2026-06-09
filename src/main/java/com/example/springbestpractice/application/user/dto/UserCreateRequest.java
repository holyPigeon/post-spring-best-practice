package com.example.springbestpractice.application.user.dto;

public record UserCreateRequest(String email, String nickname, String password) {
}
