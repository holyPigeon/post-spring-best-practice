package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.dto.PostCreateRequest;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.application.post.dto.PostUpdateRequest;
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
    public PostResponse createPost(PostCreateRequest request, LoginUser loginUser) {
        LoginUser author = requireLoginUser(loginUser);
        Post post = Post.create(request.title(), request.content(), author.id(), author.nickname());
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
    public PostResponse updatePost(Long id, PostUpdateRequest request, LoginUser loginUser) {
        Post post = getPostById(id);
        validateOwner(post, loginUser);
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    @Transactional
    public void deletePost(Long id, LoginUser loginUser) {
        Post post = getPostById(id);
        validateOwner(post, loginUser);
        commentRepository.deleteByPostId(id);
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
