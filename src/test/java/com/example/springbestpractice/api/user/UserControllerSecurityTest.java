package com.example.springbestpractice.api.user;

import com.example.springbestpractice.application.user.UserService;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.User;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.CustomUserDetailsService;
import com.example.springbestpractice.infrastructure.security.CurrentUserArgumentResolver;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
import com.example.springbestpractice.infrastructure.security.SecurityConfig;
import com.example.springbestpractice.infrastructure.security.SecurityWebMvcConfig;
import com.example.springbestpractice.support.fixture.UserFixture;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("User API security")
class UserControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/users/me allows regular user")
    void allowRegularUser() throws Exception {
        // given
        User user = UserFixture.userWithId(1L, "user@test.com", "user", "password");
        given(userService.getMyProfile(new LoginUser(1L, "user@test.com", "user", "USER")))
                .willReturn(new UserResponse(1L, "user@test.com", "user", null, null));

        // when & then
        mockMvc.perform(get("/api/users/me").with(authentication(authToken(user))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/me allows admin user")
    void allowAdminUser() throws Exception {
        // given
        User admin = UserFixture.adminWithId(2L);
        given(userService.getMyProfile(new LoginUser(2L, "admin@test.com", "admin", "ADMIN")))
                .willReturn(new UserResponse(2L, "admin@test.com", "admin", null, null));

        // when & then
        mockMvc.perform(get("/api/users/me").with(authentication(authToken(admin))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/users/me rejects anonymous user")
    void rejectAnonymousUser() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/users/me rejects invalid token")
    void rejectInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /api/users/me rejects token for missing user")
    void rejectTokenForMissingUser() throws Exception {
        // given
        Claims claims = mock(Claims.class);
        given(jwtTokenProvider.isValid("valid-token")).willReturn(true);
        given(jwtTokenProvider.parseClaims("valid-token")).willReturn(claims);
        given(claims.getSubject()).willReturn("deleted@test.com");
        given(userDetailsService.loadUserByUsername("deleted@test.com"))
                .willThrow(new UsernameNotFoundException("user not found"));

        // when & then
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer valid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    private Authentication authToken(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
