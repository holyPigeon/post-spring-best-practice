package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.post.PostLikeRedisRepository;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostLikeRedisRepository postLikeRedisRepository;

    public PostLikeResponse like(PostLikeCreateCommand command) {
        Long postId = command.postId();
        validatePostExists(postId);
        ensureMembershipLoaded(postId);
        long count = postLikeRedisRepository.like(postId, command.loginUser().id());
        return PostLikeResponse.liked(postId, count);
    }

    public PostLikeResponse unlike(PostLikeDeleteCommand command) {
        Long postId = command.postId();
        validatePostExists(postId);
        ensureMembershipLoaded(postId);
        long count = postLikeRedisRepository.unlike(postId, command.loginUser().id());
        return PostLikeResponse.unliked(postId, count);
    }

    private void ensureMembershipLoaded(Long postId) {
        postLikeRedisRepository.ensureLoaded(postId, () -> postLikeRepository.findUserIdsByPostId(postId));
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException(postId);
        }
    }
}
