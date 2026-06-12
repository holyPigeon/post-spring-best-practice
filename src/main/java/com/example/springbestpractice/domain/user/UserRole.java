package com.example.springbestpractice.domain.user;

import java.util.List;

public enum UserRole {
    USER(List.of("ROLE_USER")),
    ADMIN(List.of("ROLE_ADMIN", "ROLE_USER"));

    private final List<String> authorities;

    UserRole(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getAuthority() {
        return "ROLE_" + name();
    }

    public List<String> getAuthorities() {
        return authorities;
    }
}
