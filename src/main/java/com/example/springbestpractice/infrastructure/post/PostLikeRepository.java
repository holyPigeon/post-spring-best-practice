package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    @Modifying
    @Query("delete from PostLike pl where pl.post.id = :postId and pl.userId = :userId")
    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    @Modifying
    @Query("delete from PostLike pl where pl.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
