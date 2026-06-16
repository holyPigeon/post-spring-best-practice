package com.example.springbestpractice.api.post;

import com.example.springbestpractice.application.post.PostService;
import com.example.springbestpractice.application.post.command.PostCreateCommand;
import com.example.springbestpractice.application.post.command.PostDeleteCommand;
import com.example.springbestpractice.application.post.command.PostUpdateCommand;
import com.example.springbestpractice.application.post.dto.PostCreateRequest;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.application.post.dto.PostSort;
import com.example.springbestpractice.application.post.dto.PostUpdateRequest;
import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.model.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(PostCreateCommand.from(request, loginUser)));
    }

    @GetMapping
    public PageResponse<PostResponse> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "LATEST") PostSort sort
    ) {
        int boundedSize = Math.min(size, MAX_PAGE_SIZE);
        return postService.getPosts(PageRequest.of(page, boundedSize, sort.getSort()));
    }

    @GetMapping("/{id}")
    public PostResponse getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @PutMapping("/{id}")
    public PostResponse updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request,
            @CurrentUser LoginUser loginUser
    ) {
        return postService.updatePost(PostUpdateCommand.from(id, request, loginUser));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Long id, @CurrentUser LoginUser loginUser) {
        postService.deletePost(PostDeleteCommand.from(id, loginUser));
    }
}
