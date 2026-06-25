package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostLikeWriter postLikeWriter;

    @Transactional
    public PostLikeResponse like(PostLikeCreateCommand command) {
        LoginUser loginUser = command.loginUser();
        Post post = getPost(command.postId());
        try {
            postLikeWriter.insert(post.getId(), loginUser.id());
        } catch (DataIntegrityViolationException alreadyLiked) {
            // 유니크 제약 위반 = 이미 좋아요한 상태 → 멱등 무시
        }
        postRepository.syncLikeCount(post.getId());
        return PostLikeResponse.liked(getPost(post.getId()));
    }

    @Transactional
    public PostLikeResponse unlike(PostLikeDeleteCommand command) {
        LoginUser loginUser = command.loginUser();
        Post post = getPost(command.postId());
        postLikeRepository.deleteByPostIdAndUserId(post.getId(), loginUser.id());
        postRepository.syncLikeCount(post.getId());
        return PostLikeResponse.unliked(getPost(post.getId()));
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
