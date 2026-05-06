package com.example.springbestpractice.api.user.dto;

public record UserCreateRequest(String email, String nickname, String password) {
}