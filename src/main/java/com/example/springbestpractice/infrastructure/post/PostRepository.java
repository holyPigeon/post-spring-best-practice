package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
