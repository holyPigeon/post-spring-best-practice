package com.example.springbestpractice.domain.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("리프레시 토큰")
class RefreshTokenTest {

    @Nested
    @DisplayName("만료 여부 확인")
    class IsExpired {

        @Test
        @DisplayName("만료 시간이 충분히 남아있으면 false를 반환한다")
        void returnFalseWhenNotExpired() {
            // given
            RefreshToken token = RefreshToken.create(1L, "token", 604800000L);

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
}
