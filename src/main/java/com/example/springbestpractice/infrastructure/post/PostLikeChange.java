package com.example.springbestpractice.infrastructure.post;

/**
 * 좋아요 변경 이벤트. write-behind 워커가 DB에 영속화할 단위.
 * {@code recordId}는 Redis Stream의 엔트리 ID로, 처리 후 ack/삭제에 사용한다.
 */
public record PostLikeChange(
        PostLikeOperation operation,
        Long postId,
        Long userId,
        String recordId
) {
}
