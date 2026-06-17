package com.example.springbestpractice.domain.post;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_post_created_at_id", columnList = "created_at, id"),
        @Index(name = "idx_post_author_id_created_at_id", columnList = "author_id, created_at, id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Long authorId;

    @Column(name = "author", nullable = false)
    private String authorNickname;

    @Column(nullable = false)
    private long likeCount;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private Post(String title, String content, Long authorId, String authorNickname) {
        this.title = requireNotBlank(title, "제목은 필수입니다.");
        this.content = requireNotBlank(content, "내용은 필수입니다.");
        this.authorId = requireAuthorId(authorId);
        this.authorNickname = requireNotBlank(authorNickname, "작성자는 필수입니다.");
    }

    public static Post create(String title, String content, Long authorId, String authorNickname) {
        return new Post(title, content, authorId, authorNickname);
    }

    public void update(String title, String content) {
        this.title = requireNotBlank(title, "제목은 필수입니다.");
        this.content = requireNotBlank(content, "내용은 필수입니다.");
    }

    public boolean isWrittenBy(Long userId) {
        return authorId.equals(userId);
    }

    private static Long requireAuthorId(Long authorId) {
        if (authorId == null) {
            throw new IllegalArgumentException("작성자 아이디는 필수입니다.");
        }
        return authorId;
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}
