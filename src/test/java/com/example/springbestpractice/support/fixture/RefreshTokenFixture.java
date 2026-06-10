package com.example.springbestpractice.support.fixture;

import com.example.springbestpractice.domain.auth.RefreshToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

public final class RefreshTokenFixture {

    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    private RefreshTokenFixture() {
    }

    public static RefreshToken expiredToken(Long userId, String token) {
        RefreshToken refreshToken = RefreshToken.create(userId, token, REFRESH_EXPIRATION_MS);
        ReflectionTestUtils.setField(refreshToken, "expiresAt", LocalDateTime.now().minusSeconds(1));
        return refreshToken;
    }
}
