package com.example.springbestpractice.application.post.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record PostSearchCondition(
        @Size(max = 100, message = "keyword must be 100 characters or less.")
        String keyword,

        @Positive(message = "authorId must be positive.")
        Long authorId,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdFrom,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime createdTo
) {
    public PostSearchCondition {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isBlank()) {
                keyword = null;
            }
        }
    }

    @AssertTrue(message = "createdFrom must be before or equal to createdTo.")
    public boolean isCreatedPeriodValid() {
        return createdFrom == null || createdTo == null || !createdFrom.isAfter(createdTo);
    }
}
