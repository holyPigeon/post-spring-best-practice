package com.example.springbestpractice.api.user;

import com.example.springbestpractice.application.user.AdminUserService;
import com.example.springbestpractice.application.user.command.AdminUserDeleteCommand;
import com.example.springbestpractice.application.user.dto.AdminUserPageRequest;
import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.exception.ConflictException;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.infrastructure.security.CurrentUserArgumentResolver;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.CustomUserDetailsService;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
import com.example.springbestpractice.infrastructure.security.SecurityConfig;
import com.example.springbestpractice.infrastructure.security.SecurityWebMvcConfig;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import({SecurityConfig.class, CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("Admin user API")
class AdminUserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminUserService adminUserService;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/admin/users returns a page of users for admin")
    void getUsers() throws Exception {
        // given
        PageResponse<AdminUserResponse> page =
                new PageResponse<>(List.of(userResponse(), adminResponse()), 0, 20, 2, 1, true, true);
        given(adminUserService.getUsers(any(AdminUserPageRequest.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].role").value("USER"))
                .andExpect(jsonPath("$.content[1].role").value("ADMIN"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/admin/users returns 400 when size exceeds the limit")
    void getUsersWithTooLargeSize() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/users").param("size", "101").with(user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminUserService);
    }

    @Test
    @DisplayName("GET /api/admin/users returns forbidden for user")
    void getUsersForbiddenForUser() throws Exception {
        // when & then
        mockMvc.perform(get("/api/admin/users").with(user("user").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").exists());

        verifyNoInteractions(adminUserService);
    }

    @Test
    @DisplayName("GET /api/admin/users/{id} returns a user for admin")
    void getUser() throws Exception {
        // given
        given(adminUserService.getUser(1L)).willReturn(userResponse());

        // when & then
        mockMvc.perform(get("/api/admin/users/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }

    @Test
    @DisplayName("GET /api/admin/users/{id} returns not found")
    void getUserNotFound() throws Exception {
        // given
        given(adminUserService.getUser(999L)).willThrow(new UserNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/admin/users/999").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} deletes a user for admin")
    void deleteUser() throws Exception {
        // given
        willDoNothing().given(adminUserService).deleteUser(any(AdminUserDeleteCommand.class));

        // when & then
        mockMvc.perform(delete("/api/admin/users/1").with(admin()))
                .andExpect(status().isNoContent());

        verify(adminUserService).deleteUser(any(AdminUserDeleteCommand.class));
    }

    @Test
    @DisplayName("DELETE /api/admin/users/{id} returns 409 when deletion is not allowed")
    void deleteUserConflict() throws Exception {
        // given
        willThrow(new ConflictException("마지막 관리자는 삭제할 수 없습니다."))
                .given(adminUserService).deleteUser(any(AdminUserDeleteCommand.class));

        // when & then
        mockMvc.perform(delete("/api/admin/users/2").with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    private RequestPostProcessor admin() {
        CustomUserDetails details = new CustomUserDetails(UserFixture.adminWithId(2L));
        return authentication(new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities()));
    }

    private AdminUserResponse userResponse() {
        return new AdminUserResponse(1L, "user@test.com", "user", UserRole.USER, null, null);
    }

    private AdminUserResponse adminResponse() {
        return new AdminUserResponse(2L, "admin@test.com", "admin", UserRole.ADMIN, null, null);
    }
}
