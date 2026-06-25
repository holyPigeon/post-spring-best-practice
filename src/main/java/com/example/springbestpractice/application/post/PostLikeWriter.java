package com.example.springbestpractice.application.post;

import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostLike;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostLikeWriter {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;

    /**
     * 좋아요를 별도 트랜잭션에서 저장한다.
     *
     * <p>동시 중복 요청은 유니크 제약(uk_post_likes_post_id_user_id)에 막혀
     * {@code DataIntegrityViolationException}으로 끝난다. 이 메서드를 별도 트랜잭션으로 두면
     * 위반 시 이 트랜잭션만 롤백되고 호출자 트랜잭션은 오염되지 않으므로,
     * 호출자가 예외를 잡아 멱등하게 흡수할 수 있다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insert(Long postId, Long userId) {
        Post post = postRepository.getReferenceById(postId);
        postLikeRepository.saveAndFlush(PostLike.create(post, userId));
    }
}
