package com.example.springbestpractice.post.repository;

import com.example.springbestpractice.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
