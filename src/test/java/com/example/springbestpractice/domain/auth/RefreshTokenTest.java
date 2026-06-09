package com.example.springbestpractice.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("리프레시 토큰")
class RefreshTokenTest {

    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    @Nested
    @DisplayName("만료 여부 확인")
    class IsExpired {

        @Test
        @DisplayName("만료 시간이 충분히 남아있으면 false를 반환한다")
        void returnFalseWhenNotExpired() {
            // given
            RefreshToken token = RefreshToken.create(1L, "token", REFRESH_EXPIRATION_MS);

            // when & then
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("만료 시간이 이미 지났으면 true를 반환한다")
        void returnTrueWhenExpired() {
            // given
            RefreshToken token = RefreshToken.builder()
                    .userId(1L)
                    .token("token")
                    .expiresAt(LocalDateTime.now().minusSeconds(1))
                    .build();

            // when & then
            assertThat(token.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("생성")
    class Create {

        @Test
        @DisplayName("토큰이 비어 있으면 예외를 던진다")
        void throwExceptionWhenTokenBlank() {
            assertThatThrownBy(() -> RefreshToken.create(1L, " ", REFRESH_EXPIRATION_MS))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("리프레시 토큰은 필수입니다.");
        }

        @Test
        @DisplayName("만료 시간이 0 이하이면 예외를 던진다")
        void throwExceptionWhenExpirationNotPositive() {
            assertThatThrownBy(() -> RefreshToken.create(1L, "token", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("만료 시간은 0보다 커야 합니다.");
        }
    }
}
