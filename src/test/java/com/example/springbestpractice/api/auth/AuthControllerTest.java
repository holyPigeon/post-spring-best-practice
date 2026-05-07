package com.example.springbestpractice.api.auth;

import com.example.springbestpractice.api.auth.dto.LoginRequest;
import com.example.springbestpractice.common.exception.GlobalExceptionHandler;
import com.example.springbestpractice.common.resolver.CurrentUserArgumentResolver;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("인증 API")
class AuthControllerTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenProvider jwtTokenProvider;

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
        @DisplayName("올바른 이메일과 비밀번호로 로그인하면 200과 토큰을 반환한다")
        void loginSuccess() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "password");
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            given(authenticationManager.authenticate(any())).willReturn(auth);
            given(jwtTokenProvider.createToken(1L, "test@test.com")).willReturn("jwt-token");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("jwt-token"));
        }

        @Test
        @DisplayName("비밀번호가 틀리면 401을 반환한다")
        void loginBadCredentials() throws Exception {
            // given
            LoginRequest request = new LoginRequest("test@test.com", "wrong");
            given(authenticationManager.authenticate(any())).willThrow(new BadCredentialsException("bad credentials"));

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 올바르지 않습니다."));
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
