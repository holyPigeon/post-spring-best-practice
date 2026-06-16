package com.example.springbestpractice.api.post;

import com.example.springbestpractice.application.post.PostLikeService;
import com.example.springbestpractice.application.post.command.PostLikeCreateCommand;
import com.example.springbestpractice.application.post.command.PostLikeDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.common.annotation.CurrentUser;
import com.example.springbestpractice.common.model.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping
    public PostLikeResponse like(@PathVariable Long postId, @CurrentUser LoginUser loginUser) {
        return postLikeService.like(PostLikeCreateCommand.from(postId, loginUser));
    }

    @DeleteMapping
    public PostLikeResponse unlike(@PathVariable Long postId, @CurrentUser LoginUser loginUser) {
        return postLikeService.unlike(PostLikeDeleteCommand.from(postId, loginUser));
    }
}
