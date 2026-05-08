package com.example.springbestpractice.application.auth;

import com.example.springbestpractice.api.auth.dto.LoginRequest;
import com.example.springbestpractice.api.auth.dto.RefreshRequest;
import com.example.springbestpractice.api.auth.dto.TokenResponse;
import com.example.springbestpractice.domain.auth.ExpiredRefreshTokenException;
import com.example.springbestpractice.domain.auth.RefreshToken;
import com.example.springbestpractice.domain.auth.RefreshTokenNotFoundException;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.infrastructure.auth.RefreshTokenRepository;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
import com.example.springbestpractice.infrastructure.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("인증 서비스")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthService authService;

    private static final long REFRESH_EXPIRATION_MS = 604800000L;

    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("테스터")
                .password("encoded-password")
                .build();
        userDetails = new CustomUserDetails(user);
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("정상 로그인 시 액세스 토큰과 리프레시 토큰을 반환한다")
        void loginSuccess() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password");
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(jwtTokenProvider.createAccessToken(1L, "test@test.com")).willReturn("access-token");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(REFRESH_EXPIRATION_MS);

            // when
            TokenResponse result = authService.login(request);

            // then
            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isNotNull().isNotBlank();
            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("잘못된 인증 정보면 BadCredentialsException을 전파한다")
        void loginBadCredentials() {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "wrong");
            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("bad credentials"));

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class Refresh {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 기존 토큰을 삭제하고 새 토큰 쌍을 반환한다")
        void refreshSuccess() {
            // given
            RefreshToken refreshToken = RefreshToken.create(1L, "valid-token", REFRESH_EXPIRATION_MS);
            given(refreshTokenRepository.findByToken("valid-token")).willReturn(Optional.of(refreshToken));
            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(jwtTokenProvider.createAccessToken(1L, "test@test.com")).willReturn("new-access-token");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(REFRESH_EXPIRATION_MS);

            // when
            TokenResponse result = authService.refresh(new RefreshRequest("valid-token"));

            // then
            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isNotNull().isNotBlank();
            verify(refreshTokenRepository).delete(refreshToken);
        }

        @Test
        @DisplayName("존재하지 않는 리프레시 토큰이면 RefreshTokenNotFoundException을 던진다")
        void throwExceptionWhenTokenNotFound() {
            // given
            given(refreshTokenRepository.findByToken("invalid-token")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshRequest("invalid-token")))
                    .isInstanceOf(RefreshTokenNotFoundException.class);
        }

        @Test
        @DisplayName("만료된 리프레시 토큰이면 토큰을 삭제하고 ExpiredRefreshTokenException을 던진다")
        void throwExceptionWhenTokenExpired() {
            // given
            RefreshToken expiredToken = RefreshToken.builder()
                    .userId(1L)
                    .token("expired-token")
                    .expiresAt(LocalDateTime.now().minusSeconds(1))
                    .build();
            given(refreshTokenRepository.findByToken("expired-token")).willReturn(Optional.of(expiredToken));

            // when & then
            assertThatThrownBy(() -> authService.refresh(new RefreshRequest("expired-token")))
                    .isInstanceOf(ExpiredRefreshTokenException.class);
            verify(refreshTokenRepository).delete(expiredToken);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 로그아웃하면 해당 토큰을 삭제한다")
        void logoutSuccess() {
            // given
            RefreshToken refreshToken = RefreshToken.create(1L, "valid-token", REFRESH_EXPIRATION_MS);
            given(refreshTokenRepository.findByToken("valid-token")).willReturn(Optional.of(refreshToken));

            // when
            authService.logout(new RefreshRequest("valid-token"));

            // then
            verify(refreshTokenRepository).delete(refreshToken);
        }

        @Test
        @DisplayName("존재하지 않는 리프레시 토큰으로 로그아웃해도 예외 없이 성공한다")
        void logoutWithNonExistentToken() {
            // given
            given(refreshTokenRepository.findByToken("nonexistent-token")).willReturn(Optional.empty());

            // when & then
            assertThatNoException().isThrownBy(
                    () -> authService.logout(new RefreshRequest("nonexistent-token"))
            );
        }
    }
}
