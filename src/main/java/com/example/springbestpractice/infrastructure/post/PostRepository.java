package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Post p set p.likeCount = :count where p.id = :postId")
    int updateLikeCount(@Param("postId") Long postId, @Param("count") long count);
}
