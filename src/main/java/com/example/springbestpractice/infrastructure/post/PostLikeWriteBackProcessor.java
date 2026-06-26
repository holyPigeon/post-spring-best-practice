package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.PostLike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * write-behind 영속화의 트랜잭션 경계. 변경 이벤트를 {@code post_likes}와 {@code Post.likeCount}에 멱등하게 반영한다.
 * (스케줄러와 분리해야 self-invocation으로 인한 {@code @Transactional} 미적용을 피할 수 있다.)
 */
@Component
@RequiredArgsConstructor
public class PostLikeWriteBackProcessor {

    private final PostLikeRedisRepository postLikeRedisRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    @Transactional
    public void persist(List<PostLikeChange> changes) {
        Set<Long> affectedPostIds = new LinkedHashSet<>();
        for (PostLikeChange change : changes) {
            apply(change);
            affectedPostIds.add(change.postId());
        }
        for (Long postId : affectedPostIds) {
            postRepository.updateLikeCount(postId, postLikeRedisRepository.count(postId));
        }
    }

    private void apply(PostLikeChange change) {
        Long postId = change.postId();
        Long userId = change.userId();
        if (change.operation() == PostLikeOperation.LIKE) {
            // 멱등: 재처리 중복 INSERT 방지 + 삭제된 게시글이면 스킵(FK 위반으로 트랜잭션이 오염되지 않도록).
            if (postRepository.existsById(postId) && !postLikeRepository.existsByPostIdAndUserId(postId, userId)) {
                postLikeRepository.save(PostLike.create(postRepository.getReferenceById(postId), userId));
            }
        } else {
            // 멱등: 행이 없으면 0건 삭제로 무해.
            postLikeRepository.deleteByPostIdAndUserId(postId, userId);
        }
    }
}
