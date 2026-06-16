package com.example.springbestpractice.application.post.dto;

import com.example.springbestpractice.domain.post.Post;

public record PostLikeResponse(
        Long postId,
        long likeCount,
        boolean liked
) {
    public static PostLikeResponse liked(Post post) {
        return new PostLikeResponse(post.getId(), post.getLikeCount(), true);
    }

    public static PostLikeResponse unliked(Post post) {
        return new PostLikeResponse(post.getId(), post.getLikeCount(), false);
    }
}
