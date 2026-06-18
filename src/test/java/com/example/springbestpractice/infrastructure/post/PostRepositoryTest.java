package com.example.springbestpractice.infrastructure.post;

import com.example.springbestpractice.domain.post.Post;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;

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

    @Test
    @DisplayName("search posts by title keyword")
    void searchByTitleKeyword() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 10, 12, 0);
        persistPost("Spring QueryDSL", 1L, baseTime.minusDays(1));
        persistPost("JPA basics", 1L, baseTime.minusDays(2));

        // when
        Page<Post> result = postRepository.search("Query", null, null, null, null, latestPage(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .containsExactly("Spring QueryDSL");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("search posts by content keyword")
    void searchByContentKeyword() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 10, 12, 0);
        persistPost("Spring title", "QueryDSL predicate content", 1L, baseTime.minusDays(1));
        persistPost("JPA basics", "Entity mapping content", 1L, baseTime.minusDays(2));

        // when
        Page<Post> result = postRepository.search(null, "predicate", null, null, null, latestPage(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .containsExactly("Spring title");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("search posts by author and created period")
    void searchByAuthorAndCreatedPeriod() {
        // given
        LocalDateTime start = LocalDateTime.of(2026, 1, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 1, 20, 23, 59);
        persistPost("before period", 1L, start.minusSeconds(1));
        persistPost("matching post", 1L, start.plusDays(1));
        persistPost("different author", 2L, start.plusDays(1));
        persistPost("after period", 1L, end.plusSeconds(1));

        // when
        Page<Post> result = postRepository.search(null, null, 1L, start, end, latestPage(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .containsExactly("matching post");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("search posts with offset paging")
    void searchWithOffsetPaging() {
        // given
        LocalDateTime baseTime = LocalDateTime.of(2026, 1, 10, 12, 0);
        persistPost("old", 1L, baseTime.minusDays(2));
        persistPost("middle", 1L, baseTime.minusDays(1));
        persistPost("new", 1L, baseTime);

        // when
        Page<Post> result = postRepository.search(null, null, null, null, null, latestPage(1, 2));

        // then
        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .containsExactly("old");
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("search posts with id tie-breaker when createdAt is same")
    void searchWithIdTieBreaker() {
        // given
        LocalDateTime sameTime = LocalDateTime.of(2026, 1, 10, 12, 0);
        persistPost("first", 1L, sameTime);
        persistPost("second", 1L, sameTime);

        // when
        Page<Post> result = postRepository.search(null, null, null, null, null, latestPage(0, 10));

        // then
        assertThat(result.getContent())
                .extracting(Post::getTitle)
                .containsExactly("second", "first");
    }

    private PageRequest latestPage(int page, int size) {
        return PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));
    }

    private Post persistPost(String title, Long authorId, LocalDateTime createdAt) {
        return persistPost(title, "content", authorId, createdAt);
    }

    private Post persistPost(String title, String content, Long authorId, LocalDateTime createdAt) {
        Post post = em.persist(Post.create(title, content, authorId, "writer" + authorId));
        em.flush();
        em.getEntityManager().createQuery("update Post p set p.createdAt = :createdAt where p.id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", post.getId())
                .executeUpdate();
        em.clear();
        return post;
    }
}
