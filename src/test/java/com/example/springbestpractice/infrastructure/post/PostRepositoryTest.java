package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("Post repository")
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("increase like count atomically")
    void increaseLikeCount() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        em.flush();
        em.clear();

        // when
        int updatedCount = postRepository.increaseLikeCount(post.getId());
        em.flush();
        em.clear();

        // then
        assertThat(updatedCount).isEqualTo(1);
        assertThat(postRepository.findById(post.getId()).orElseThrow().getLikeCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("decrease like count atomically")
    void decreaseLikeCount() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        em.flush();
        em.clear();
        postRepository.increaseLikeCount(post.getId());
        em.flush();
        em.clear();

        // when
        int updatedCount = postRepository.decreaseLikeCount(post.getId());
        em.flush();
        em.clear();

        // then
        assertThat(updatedCount).isEqualTo(1);
        assertThat(postRepository.findById(post.getId()).orElseThrow().getLikeCount()).isZero();
    }

    @Test
    @DisplayName("do not decrease like count below zero")
    void doNotDecreaseLikeCountBelowZero() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        em.flush();
        em.clear();

        // when
        int updatedCount = postRepository.decreaseLikeCount(post.getId());
        em.flush();
        em.clear();

        // then
        assertThat(updatedCount).isZero();
        assertThat(postRepository.findById(post.getId()).orElseThrow().getLikeCount()).isZero();
    }
}
