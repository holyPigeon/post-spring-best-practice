package com.example.springbestpractice.domain.post;

import jakarta.persistence.*;
import lombok.*;
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

    @Column(nullable = false)
    private String author;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Post create(String title, String content, Long authorId, String author) {
        return Post.builder()
                .title(requireNotBlank(title, "제목은 필수입니다."))
                .content(requireNotBlank(content, "내용은 필수입니다."))
                .authorId(requireAuthorId(authorId))
                .author(requireNotBlank(author, "작성자는 필수입니다."))
                .build();
    }

    public void update(String title, String content) {
        this.title = requireNotBlank(title, "제목은 필수입니다.");
        this.content = requireNotBlank(content, "내용은 필수입니다.");
    }

    public boolean isWrittenBy(Long userId) {
        return authorId != null && authorId.equals(userId);
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
