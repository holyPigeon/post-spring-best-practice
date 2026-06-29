package com.example.springbestpractice.application.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AdminUserPageRequest(
        @Min(0) @Max(1000) Integer page,
        @Min(1) @Max(100) Integer size
) {
    public AdminUserPageRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }
}
