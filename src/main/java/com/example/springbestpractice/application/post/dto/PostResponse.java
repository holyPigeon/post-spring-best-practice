package com.example.springbestpractice.application.post.dto;

import com.example.springbestpractice.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorNickname(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
