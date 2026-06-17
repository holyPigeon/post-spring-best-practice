package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostLike;
import com.example.springbestpractice.domain.post.PostNotFoundException;
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

    @Transactional
    public PostLikeResponse like(PostLikeCreateCommand command) {
        LoginUser loginUser = command.loginUser();
        Post post = getPost(command.postId());
        if (postLikeRepository.existsByPostIdAndUserId(post.getId(), loginUser.id())) {
            return PostLikeResponse.liked(post);
        }

        postLikeRepository.save(PostLike.create(post, loginUser.id()));
        postRepository.increaseLikeCount(post.getId());
        return PostLikeResponse.liked(getPost(post.getId()));
    }

    @Transactional
    public PostLikeResponse unlike(PostLikeDeleteCommand command) {
        LoginUser loginUser = command.loginUser();
        Post post = getPost(command.postId());
        int deletedCount = postLikeRepository.deleteByPostIdAndUserId(post.getId(), loginUser.id());
        if (deletedCount > 0) {
            postRepository.decreaseLikeCount(post.getId());
            return PostLikeResponse.unliked(getPost(post.getId()));
        }
        return PostLikeResponse.unliked(post);
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }
}
