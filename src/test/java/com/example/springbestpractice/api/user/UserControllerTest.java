package com.example.springbestpractice.api.user;

import com.example.springbestpractice.application.user.UserService;
import com.example.springbestpractice.application.user.command.UserDeleteCommand;
import com.example.springbestpractice.application.user.command.UserPasswordUpdateCommand;
import com.example.springbestpractice.application.user.command.UserUpdateCommand;
import com.example.springbestpractice.application.user.dto.UserCreateRequest;
import com.example.springbestpractice.application.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.application.user.dto.UserResponse;
import com.example.springbestpractice.application.user.dto.UserUpdateRequest;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.CurrentUserArgumentResolver;
import com.example.springbestpractice.infrastructure.security.SecurityWebMvcConfig;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("User API")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    @BeforeEach
    void setSecurityContext() {
        CustomUserDetails userDetails = new CustomUserDetails(UserFixture.userWithId(1L));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/users creates a user")
    void createUser() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("test@test.com", "tester", "password");
        given(userService.createUser(any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("tester"));
    }

    @Test
    @DisplayName("POST /api/users returns conflict for duplicate email")
    void createUserDuplicateEmail() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("test@test.com", "tester", "password");
        given(userService.createUser(any())).willThrow(new DuplicateEmailException("test@test.com"));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /api/users returns bad request for invalid email")
    void createUserInvalidEmail() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("invalid-email", "tester", "password");

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("GET /api/users/me returns the current user")
    void getMyProfile() throws Exception {
        // given
        given(userService.getMyProfile(loginUser())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        verify(userService).getMyProfile(loginUser());
    }

    @Test
    @DisplayName("PUT /api/users/me updates the current user")
    void updateMyProfile() throws Exception {
        // given
        UserUpdateRequest request = new UserUpdateRequest("updated");
        UserResponse response = new UserResponse(1L, "test@test.com", "updated", null, null);
        given(userService.updateMyProfile(any(UserUpdateCommand.class))).willReturn(response);

        // when & then
        mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("updated"));

        verify(userService).updateMyProfile(UserUpdateCommand.from(request, loginUser()));
    }

    @Test
    @DisplayName("PATCH /api/users/me/password updates the current user's password")
    void updatePassword() throws Exception {
        // given
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
        willDoNothing().given(userService).updatePassword(any(UserPasswordUpdateCommand.class));

        // when & then
        mockMvc.perform(patch("/api/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).updatePassword(UserPasswordUpdateCommand.from(request, loginUser()));
    }

    @Test
    @DisplayName("DELETE /api/users/me deletes the current user")
    void deleteMyAccount() throws Exception {
        // given
        willDoNothing().given(userService).deleteMyAccount(any(UserDeleteCommand.class));

        // when & then
        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isNoContent());

        verify(userService).deleteMyAccount(UserDeleteCommand.from(loginUser()));
    }

    private UserResponse sampleResponse() {
        return new UserResponse(1L, "test@test.com", "tester", null, null);
    }

    private LoginUser loginUser() {
        return new LoginUser(1L, "test@test.com", "tester", UserRole.USER);
    }
}
