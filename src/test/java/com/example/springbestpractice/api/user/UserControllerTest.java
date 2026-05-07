package com.example.springbestpractice.api.user;

import com.example.springbestpractice.api.user.dto.UserCreateRequest;
import com.example.springbestpractice.api.user.dto.UserPasswordUpdateRequest;
import com.example.springbestpractice.api.user.dto.UserResponse;
import com.example.springbestpractice.api.user.dto.UserUpdateRequest;
import com.example.springbestpractice.application.user.UserService;
import com.example.springbestpractice.domain.user.DuplicateEmailException;
import com.example.springbestpractice.domain.user.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("유저 API")
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;

    private UserResponse sampleResponse() {
        return new UserResponse(1L, "test@test.com", "테스터", null, null);
    }

    @Test
    @DisplayName("POST /api/users - 201과 생성된 유저를 반환한다")
    void createUser() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("test@test.com", "테스터", "password");
        given(userService.createUser(any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.nickname").value("테스터"));
    }

    @Test
    @DisplayName("POST /api/users - 이메일이 중복이면 409를 반환한다")
    void createUserDuplicateEmail() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest("test@test.com", "테스터", "password");
        given(userService.createUser(any())).willThrow(new DuplicateEmailException("test@test.com"));

        // when & then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다. email=test@test.com"));
    }

    @Test
    @DisplayName("GET /api/users - 200과 유저 목록을 반환한다")
    void getAllUsers() throws Exception {
        // given
        given(userService.getAllUsers()).willReturn(List.of(sampleResponse()));

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value("test@test.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 존재하는 ID면 200과 유저를 반환한다")
    void getUser() throws Exception {
        // given
        given(userService.getUser(1L)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 존재하지 않는 ID면 404를 반환한다")
    void getUserNotFound() throws Exception {
        // given
        given(userService.getUser(999L)).willThrow(new UserNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("유저를 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/password - 204를 반환한다")
    void updatePassword() throws Exception {
        // given
        UserPasswordUpdateRequest request = new UserPasswordUpdateRequest("newpassword");
        willDoNothing().given(userService).updatePassword(eq(1L), any());

        // when & then
        mockMvc.perform(patch("/api/users/1/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - 204를 반환한다")
    void deleteUser() throws Exception {
        // given
        willDoNothing().given(userService).deleteUser(1L);

        // when & then
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }
}
