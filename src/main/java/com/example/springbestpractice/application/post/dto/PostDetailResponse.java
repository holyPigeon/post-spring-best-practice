package com.example.springbestpractice.application.post.dto;

import com.example.springbestpractice.domain.post.Post;

import java.time.LocalDateTime;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String author,
        long likeCount,
        boolean liked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostDetailResponse from(Post post, boolean liked) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorNickname(),
                post.getLikeCount(),
                liked,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
