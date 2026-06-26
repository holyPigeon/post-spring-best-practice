package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostCreateCommand;
import com.example.springbestpractice.application.post.command.PostDeleteCommand;
import com.example.springbestpractice.application.post.command.PostUpdateCommand;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.application.post.query.PostSearchQuery;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostLikeRedisRepository;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import lombok.RequiredArgsConstructor;
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
    private final PostLikeRedisRepository postLikeRedisRepository;

    @Transactional
    public PostResponse createPost(PostCreateCommand command) {
        LoginUser author = command.loginUser();
        Post post = Post.create(command.title(), command.content(), author.id(), author.nickname());
        return PostResponse.from(postRepository.save(post));
    }

    public PostResponse getPost(Long id) {
        Post post = getPostById(id);
        postLikeRedisRepository.ensureLoaded(id, () -> postLikeRepository.findUserIdsByPostId(id));
        return PostResponse.of(post, postLikeRedisRepository.count(id));
    }

    public PageResponse<PostResponse> getPosts(PostSearchQuery query) {
        return PageResponse.from(postRepository.search(
                query.keyword(),
                query.authorId(),
                query.createdFrom(),
                query.createdTo(),
                query.pageable()
        ).map(PostResponse::from));
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
        postLikeRedisRepository.evict(command.id());
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
