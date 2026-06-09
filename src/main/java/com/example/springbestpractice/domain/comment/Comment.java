package com.example.springbestpractice.domain.comment;

import com.example.springbestpractice.domain.post.Post;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private Long authorId;

    @Column(nullable = false)
    private String author;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Comment create(Post post, String content, Long authorId, String author) {
        return Comment.builder()
                .post(requirePost(post))
                .content(requireNotBlank(content, "댓글 내용은 필수입니다."))
                .authorId(requireAuthorId(authorId))
                .author(requireNotBlank(author, "작성자는 필수입니다."))
                .build();
    }

    public void updateContent(String content) {
        this.content = requireNotBlank(content, "댓글 내용은 필수입니다.");
    }

    public boolean isWrittenBy(Long userId) {
        return authorId != null && authorId.equals(userId);
    }

    private static Post requirePost(Post post) {
        if (post == null) {
            throw new IllegalArgumentException("게시글은 필수입니다.");
        }
        return post;
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
