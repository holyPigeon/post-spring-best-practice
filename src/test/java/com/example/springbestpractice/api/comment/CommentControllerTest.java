package com.example.springbestpractice.api.comment;

import com.example.springbestpractice.application.comment.CommentService;
import com.example.springbestpractice.application.comment.command.CommentDeleteCommand;
import com.example.springbestpractice.application.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.application.comment.dto.CommentResponse;
import com.example.springbestpractice.application.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.domain.comment.CommentNotFoundException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("댓글 API")
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    @BeforeEach
    void setSecurityContext() {
        CustomUserDetails userDetails = new CustomUserDetails(UserFixture.userWithId(2L));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/comments - 생성된 댓글을 반환한다")
    void createComment() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용");
        given(commentService.createComment(any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("댓글 내용"));
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments - 댓글 목록을 반환한다")
    void getComments() throws Exception {
        // given
        given(commentService.getComments(1L)).willReturn(List.of(sampleResponse()));

        // when & then
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments/{commentId} - 없으면 404를 반환한다")
    void getCommentNotFound() throws Exception {
        // given
        given(commentService.getComment(1L, 999L)).willThrow(new CommentNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/posts/1/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/posts/{postId}/comments/{commentId} - 수정된 댓글을 반환한다")
    void updateComment() throws Exception {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("새 댓글 내용");
        given(commentService.updateComment(any())).willReturn(new CommentResponse(1L, 1L, "새 댓글 내용", "댓글 작성자", null, null));

        // when & then
        mockMvc.perform(put("/api/posts/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("새 댓글 내용"));
    }

    @Test
    @DisplayName("DELETE /api/posts/{postId}/comments/{commentId} - 204를 반환한다")
    void deleteComment() throws Exception {
        // given
        willDoNothing().given(commentService).deleteComment(any(CommentDeleteCommand.class));

        // when & then
        mockMvc.perform(delete("/api/posts/1/comments/1"))
                .andExpect(status().isNoContent());
    }

    private CommentResponse sampleResponse() {
        return new CommentResponse(1L, 1L, "댓글 내용", "댓글 작성자", null, null);
    }
}
