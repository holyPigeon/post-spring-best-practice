package com.example.springbestpractice.infrastructure.auth;

import com.example.springbestpractice.domain.auth.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("리프레시 토큰 레포지토리")
class RefreshTokenRepositoryTest {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    TestEntityManager em;

    @Nested
    @DisplayName("토큰으로 조회")
    class FindByToken {

        @Test
        @DisplayName("저장된 토큰으로 조회하면 리프레시 토큰을 반환한다")
        void returnRefreshTokenWhenExists() {
            // given
            em.persist(RefreshToken.create(1L, "test-token", 604800000L));
            em.flush();

            // when
            Optional<RefreshToken> result = refreshTokenRepository.findByToken("test-token");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getToken()).isEqualTo("test-token");
            assertThat(result.get().getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("저장되지 않은 토큰으로 조회하면 빈 값을 반환한다")
        void returnEmptyWhenNotExists() {
            // when
            Optional<RefreshToken> result = refreshTokenRepository.findByToken("nonexistent-token");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("userId로 삭제")
    class DeleteByUserId {

        @Test
        @DisplayName("해당 userId의 모든 리프레시 토큰을 삭제한다")
        void deleteAllTokensOfUser() {
            // given
            em.persist(RefreshToken.create(1L, "token-1", 604800000L));
            em.persist(RefreshToken.create(1L, "token-2", 604800000L));
            em.persist(RefreshToken.create(2L, "token-3", 604800000L));
            em.flush();

            // when
            refreshTokenRepository.deleteByUserId(1L);
            em.flush();
            em.clear();

            // then
            assertThat(refreshTokenRepository.findByToken("token-1")).isEmpty();
            assertThat(refreshTokenRepository.findByToken("token-2")).isEmpty();
            assertThat(refreshTokenRepository.findByToken("token-3")).isPresent();
        }
    }
}
