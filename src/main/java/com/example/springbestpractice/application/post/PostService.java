package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostCreateCommand;
import com.example.springbestpractice.application.post.command.PostDeleteCommand;
import com.example.springbestpractice.application.post.command.PostUpdateCommand;
import com.example.springbestpractice.application.post.dto.PostDetailResponse;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;

    @Transactional
    public PostResponse createPost(PostCreateCommand command) {
        LoginUser author = command.loginUser();
        Post post = Post.create(command.title(), command.content(), author.id(), author.nickname());
        return PostResponse.from(postRepository.save(post));
    }

    public PostDetailResponse getPost(Long id, LoginUser loginUser) {
        Post post = getPostById(id);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(id, loginUser.id());
        return PostDetailResponse.from(post, liked);
    }

    public PageResponse<PostResponse> getPosts(Pageable pageable) {
        return PageResponse.from(postRepository.findAll(pageable).map(PostResponse::from));
    }

    @Transactional
    public PostResponse updatePost(PostUpdateCommand command) {
        Post post = getPostById(command.id());
        validateOwner(post, command.loginUser());
        post.update(command.title(), command.content());
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(PostDeleteCommand command) {
        Post post = getPostById(command.id());
        validateOwner(post, command.loginUser());
        postLikeRepository.deleteByPostId(command.id());
        commentRepository.deleteByPostId(command.id());
        postRepository.delete(post);
    }

    private Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    private void validateOwner(Post post, LoginUser loginUser) {
        if (!post.isWrittenBy(loginUser.id())) {
            throw new AccessDeniedException("게시글 소유자가 아닙니다.");
        }
    }
}
