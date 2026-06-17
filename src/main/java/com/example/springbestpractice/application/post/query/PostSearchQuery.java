package com.example.springbestpractice.application.post.query;

import com.example.springbestpractice.application.post.dto.PostPageRequest;
import com.example.springbestpractice.application.post.dto.PostSearchCondition;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public record PostSearchQuery(
        String keyword,
        Long authorId,
        LocalDateTime createdFrom,
        LocalDateTime createdTo,
        Pageable pageable
) {
    public static PostSearchQuery from(PostSearchCondition condition, PostPageRequest pageRequest) {
        return new PostSearchQuery(
                condition.keyword(),
                condition.authorId(),
                condition.createdFrom(),
                condition.createdTo(),
                PageRequest.of(pageRequest.page(), pageRequest.size(), pageRequest.sort().getSort())
        );
    }
}
