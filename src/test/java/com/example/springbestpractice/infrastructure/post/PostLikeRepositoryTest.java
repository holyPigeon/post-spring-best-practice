package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostLike;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("Post like repository")
class PostLikeRepositoryTest {

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("return true when like exists")
    void returnTrueWhenLikeExists() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        em.persist(PostLike.create(post, 2L));
        em.flush();
        em.clear();

        // when & then
        assertThat(postLikeRepository.existsByPostIdAndUserId(post.getId(), 2L)).isTrue();
        assertThat(postLikeRepository.existsByPostIdAndUserId(post.getId(), 3L)).isFalse();
    }

    @Test
    @DisplayName("delete like by post id and user id")
    void deleteByPostIdAndUserId() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        PostLike postLike = em.persist(PostLike.create(post, 2L));
        PostLike another = em.persist(PostLike.create(post, 3L));
        em.flush();
        em.clear();

        // when
        int deletedCount = postLikeRepository.deleteByPostIdAndUserId(post.getId(), 2L);
        em.flush();
        em.clear();

        // then
        assertThat(deletedCount).isEqualTo(1);
        assertThat(postLikeRepository.findById(postLike.getId())).isEmpty();
        assertThat(postLikeRepository.findById(another.getId())).isPresent();
    }

    @Test
    @DisplayName("prevent duplicate likes for same post and user")
    void preventDuplicateLikes() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        em.persist(PostLike.create(post, 2L));
        em.flush();

        // when & then
        assertThatThrownBy(() -> postLikeRepository.saveAndFlush(PostLike.create(post, 2L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("delete likes by post id")
    void deleteByPostId() {
        // given
        Post post = em.persist(Post.create("title", "content", 1L, "writer"));
        Post anotherPost = em.persist(Post.create("another title", "another content", 1L, "writer"));
        PostLike first = em.persist(PostLike.create(post, 2L));
        PostLike second = em.persist(PostLike.create(post, 3L));
        PostLike another = em.persist(PostLike.create(anotherPost, 2L));
        em.flush();
        em.clear();

        // when
        postLikeRepository.deleteByPostId(post.getId());
        em.flush();
        em.clear();

        // then
        assertThat(postLikeRepository.findById(first.getId())).isEmpty();
        assertThat(postLikeRepository.findById(second.getId())).isEmpty();
        assertThat(postLikeRepository.findById(another.getId())).isPresent();
    }
}
