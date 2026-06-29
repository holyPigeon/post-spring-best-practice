package com.example.springbestpractice.application.post.dto;

public record PostLikeResponse(
        Long postId,
        long likeCount,
        boolean liked
) {
    public static PostLikeResponse liked(Long postId, long likeCount) {
        return new PostLikeResponse(postId, likeCount, true);
    }

    public static PostLikeResponse unliked(Long postId, long likeCount) {
        return new PostLikeResponse(postId, likeCount, false);
    }
}
