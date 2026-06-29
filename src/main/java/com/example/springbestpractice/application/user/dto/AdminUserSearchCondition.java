package com.example.springbestpractice.application.user.dto;

import com.example.springbestpractice.domain.user.UserRole;
import jakarta.validation.constraints.Size;

public record AdminUserSearchCondition(
        @Size(max = 100, message = "keyword must be 100 characters or less.")
        String keyword,

        UserRole role
) {
    public AdminUserSearchCondition {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isBlank()) {
                keyword = null;
            }
        }
    }
}
