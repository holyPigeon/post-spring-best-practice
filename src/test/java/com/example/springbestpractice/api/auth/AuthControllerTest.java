package com.example.springbestpractice.api.auth;

import com.example.springbestpractice.api.auth.dto.LoginRequest;
import com.example.springbestpractice.api.auth.dto.RefreshRequest;
import com.example.springbestpractice.api.auth.dto.TokenResponse;
import com.example.springbestpractice.application.auth.AuthService;
import com.example.springbestpractice.common.exception.GlobalExceptionHandler;
import com.example.springbestpractice.common.resolver.CurrentUserArgumentResolver;
import com.example.springbestpractice.domain.auth.RefreshTokenNotFoundException;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 API")
class AuthControllerTest {

    @Mock
    AuthService authService;

    @InjectMocks
    AuthController authController;

    MockMvc mockMvc;
    ObjectMapper objectMapper = new ObjectMapper();

    private User testUser;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .build();

        testUser = User.builder()
                .id(1L)
                .email("test@test.com")
                .nickname("테스터")
                .password("encoded-password")
                .build();
        userDetails = new CustomUserDetails(testUser);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("올바른 이메일과 비밀번호로 로그인하면 200과 토큰 쌍을 반환한다")
        void loginSuccess() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password");
            given(authService.login(any())).willReturn(TokenResponse.of("access-token", "refresh-token"));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
        }

        @Test
        @DisplayName("비밀번호가 틀리면 401을 반환한다")
        void loginBadCredentials() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "wrong");
            given(authService.login(any())).willThrow(new BadCredentialsException("bad credentials"));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class Refresh {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 요청하면 200과 새 토큰 쌍을 반환한다")
        void refreshSuccess() throws Exception {
            // given
            RefreshRequest request = new RefreshRequest("valid-refresh-token");
            given(authService.refresh(any())).willReturn(TokenResponse.of("new-access-token", "new-refresh-token"));

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
        }

        @Test
        @DisplayName("유효하지 않은 리프레시 토큰이면 401을 반환한다")
        void refreshWithInvalidToken() throws Exception {
            // given
            RefreshRequest request = new RefreshRequest("invalid-token");
            given(authService.refresh(any())).willThrow(new RefreshTokenNotFoundException());

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("로그아웃 요청 시 204를 반환한다")
        void logoutSuccess() throws Exception {
            // given
            RefreshRequest request = new RefreshRequest("some-refresh-token");
            willDoNothing().given(authService).logout(any());

            // when & then
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("내 정보 조회")
    class Me {

        @BeforeEach
        void setSecurityContext() {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );
            SecurityContextHolder.setContext(context);
        }

        @Test
        @DisplayName("인증된 사용자의 정보를 반환한다")
        void returnLoginUserInfo() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@test.com"))
                    .andExpect(jsonPath("$.nickname").value("테스터"));
        }
    }
}
