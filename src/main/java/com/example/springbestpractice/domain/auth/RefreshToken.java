package com.example.springbestpractice.domain.auth;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    public static RefreshToken create(Long userId, String token, long expirationMillis) {
        return RefreshToken.builder()
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusNanos(expirationMillis * 1_000_000L))
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
