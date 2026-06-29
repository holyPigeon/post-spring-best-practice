package com.example.springbestpractice.api.user;

import com.example.springbestpractice.application.user.AdminUserService;
import com.example.springbestpractice.application.user.dto.AdminUserResponse;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.infrastructure.security.CustomUserDetailsService;
import com.example.springbestpractice.infrastructure.security.JwtTokenProvider;
import com.example.springbestpractice.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@Import(SecurityConfig.class)
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
    @DisplayName("GET /api/admin/users returns users for admin")
    void getUsers() throws Exception {
        // given
        given(adminUserService.getUsers()).willReturn(List.of(userResponse(), adminResponse()));

        // when & then
        mockMvc.perform(get("/api/admin/users").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].role").value("USER"))
                .andExpect(jsonPath("$[1].role").value("ADMIN"));
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
        willDoNothing().given(adminUserService).deleteUser(1L);

        // when & then
        mockMvc.perform(delete("/api/admin/users/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(adminUserService).deleteUser(1L);
    }

    private AdminUserResponse userResponse() {
        return new AdminUserResponse(1L, "user@test.com", "user", UserRole.USER, null, null);
    }

    private AdminUserResponse adminResponse() {
        return new AdminUserResponse(2L, "admin@test.com", "admin", UserRole.ADMIN, null, null);
    }
}
