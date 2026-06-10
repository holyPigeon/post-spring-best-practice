package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostCreateCommand;
import com.example.springbestpractice.application.post.command.PostDeleteCommand;
import com.example.springbestpractice.application.post.command.PostUpdateCommand;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public PostResponse createPost(PostCreateCommand command) {
        LoginUser author = requireLoginUser(command.loginUser());
        Post post = Post.create(command.title(), command.content(), author.id(), author.nickname());
        return PostResponse.from(postRepository.save(post));
    }

    public PostResponse getPost(Long id) {
        Post post = getPostById(id);
        return PostResponse.from(post);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponse::from)
                .toList();
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
        commentRepository.deleteByPostId(command.id());
        postRepository.delete(post);
    }

    private Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    private void validateOwner(Post post, LoginUser loginUser) {
        LoginUser owner = requireLoginUser(loginUser);
        if (!post.isWrittenBy(owner.id())) {
            throw new AccessDeniedException("게시글 소유자가 아닙니다.");
        }
    }

    private LoginUser requireLoginUser(LoginUser loginUser) {
        if (loginUser == null) {
            throw new AccessDeniedException("인증된 사용자 정보가 필요합니다.");
        }
        return loginUser;
    }
}
