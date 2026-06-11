package com.example.springbestpractice.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    private RefreshToken(Long userId, String token, long expirationMillis) {
        this.userId = requireUserId(userId);
        this.token = requireNotBlank(token, "리프레시 토큰은 필수입니다.");
        this.expiresAt = LocalDateTime.now().plusNanos(requirePositiveExpiration(expirationMillis) * 1_000_000L);
    }

    public static RefreshToken create(Long userId, String token, long expirationMillis) {
        return new RefreshToken(userId, token, expirationMillis);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    private static Long requireUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("유저 ID는 필수입니다.");
        }
        return userId;
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static long requirePositiveExpiration(long expirationMillis) {
        if (expirationMillis <= 0) {
            throw new IllegalArgumentException("만료 시간은 0보다 커야 합니다.");
        }
        return expirationMillis;
    }
}
