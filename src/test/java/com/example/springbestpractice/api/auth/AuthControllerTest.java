package com.example.springbestpractice.api.auth;

import com.example.springbestpractice.application.auth.AuthService;
import com.example.springbestpractice.application.auth.dto.LoginRequest;
import com.example.springbestpractice.application.auth.dto.RefreshRequest;
import com.example.springbestpractice.application.auth.dto.TokenResponse;
import com.example.springbestpractice.domain.auth.RefreshTokenNotFoundException;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.CurrentUserArgumentResolver;
import com.example.springbestpractice.infrastructure.security.SecurityWebMvcConfig;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("인증 API")
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        @Test
        @DisplayName("올바른 정보면 토큰 쌍을 반환한다")
        void loginSuccess() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password");
            given(authService.login(any())).willReturn(TokenResponse.of("access-token", "refresh-token"));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"));
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
                    .andExpect(status().isUnauthorized());
        }
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
                .andExpect(status().isUnauthorized());
    }

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

    @Nested
    @DisplayName("내 정보 조회")
    class Me {

        @BeforeEach
        void setSecurityContext() {
            CustomUserDetails userDetails = new CustomUserDetails(UserFixture.userWithId(1L));
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
            );
        }

        @Test
        @DisplayName("인증된 사용자의 정보를 반환한다")
        void returnLoginUserInfo() throws Exception {
            mockMvc.perform(get("/api/auth/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.email").value("test@test.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }
    }
}
