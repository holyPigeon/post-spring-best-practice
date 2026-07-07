package com.example.springbestpractice.common.model;

import com.example.springbestpractice.domain.user.UserRole;

public record LoginUser(Long id, String email, String nickname, UserRole role) {
}
