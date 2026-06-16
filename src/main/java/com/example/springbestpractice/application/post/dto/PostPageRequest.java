package com.example.springbestpractice.application.post.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PostPageRequest(
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size,
        PostSort sort
) {
    public PostPageRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
        if (sort == null) {
            sort = PostSort.LATEST;
        }
    }
}
