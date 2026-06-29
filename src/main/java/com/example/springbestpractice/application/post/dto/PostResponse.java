package com.example.springbestpractice.application.post.dto;

import com.example.springbestpractice.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        String author,
        long likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return of(post, post.getLikeCount());
    }

    public static PostResponse of(Post post, long likeCount) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorNickname(),
                likeCount,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
