package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface PostRepositoryCustom {

    Page<Post> search(String keyword, Long authorId, LocalDateTime createdFrom, LocalDateTime createdTo, Pageable pageable);
}
