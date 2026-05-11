package com.example.springbestpractice.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JWT 토큰 프로바이더")
class JwtTokenProviderTest {

    private static final String SECRET = "spring-best-practice-jwt-secret-key-must-be-256bits-or-longer-than-that";
    private static final long ACCESS_EXPIRATION = 1800000L;
    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION_MS);
    }

    @Nested
    @DisplayName("액세스 토큰 생성")
    class CreateAccessToken {

        @Test
        @DisplayName("userId와 email로 JWT를 생성하고 claims를 확인한다")
        void createTokenWithClaims() {
            // when
            String token = jwtTokenProvider.createAccessToken(1L, "test@test.com");

            // then
            assertThat(token).isNotNull().isNotBlank();
            Claims claims = jwtTokenProvider.parseClaims(token);
            assertThat(claims.getSubject()).isEqualTo("test@test.com");
            assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("토큰 유효성 검사")
    class IsValid {

        @Test
        @DisplayName("유효한 토큰이면 true를 반환한다")
        void returnTrueForValidToken() {
            // given
            String token = jwtTokenProvider.createAccessToken(1L, "test@test.com");

            // when & then
            assertThat(jwtTokenProvider.isValid(token)).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰이면 false를 반환한다")
        void returnFalseForExpiredToken() {
            // given
            JwtTokenProvider shortLived = new JwtTokenProvider(SECRET, 1L, REFRESH_EXPIRATION_MS);
            String token = shortLived.createAccessToken(1L, "test@test.com");

            // when & then
            assertThat(shortLived.isValid(token)).isFalse();
        }

        @Test
        @DisplayName("변조된 토큰이면 false를 반환한다")
        void returnFalseForTamperedToken() {
            // given
            String tampered = "eyJhbGciOiJIUzI1NiJ9.tampered.signature";

            // when & then
            assertThat(jwtTokenProvider.isValid(tampered)).isFalse();
        }

        @Test
        @DisplayName("형식이 잘못된 문자열이면 false를 반환한다")
        void returnFalseForMalformedString() {
            assertThat(jwtTokenProvider.isValid("not-a-jwt")).isFalse();
        }
    }
}
