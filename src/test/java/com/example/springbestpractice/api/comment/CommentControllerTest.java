package com.example.springbestpractice.api.comment;

import com.example.springbestpractice.api.comment.dto.CommentCreateRequest;
import com.example.springbestpractice.api.comment.dto.CommentResponse;
import com.example.springbestpractice.api.comment.dto.CommentUpdateRequest;
import com.example.springbestpractice.application.comment.CommentService;
import com.example.springbestpractice.domain.comment.CommentNotFoundException;
import com.example.springbestpractice.domain.post.PostNotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@DisplayName("댓글 API")
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    private CommentResponse sampleResponse() {
        return new CommentResponse(1L, 1L, "댓글 내용", "댓글 작성자", null, null);
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/comments - 201과 생성된 댓글을 반환한다")
    void createComment() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest("댓글 내용", "댓글 작성자");
        given(commentService.createComment(eq(1L), any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/posts/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.postId").value(1L))
                .andExpect(jsonPath("$.content").value("댓글 내용"))
                .andExpect(jsonPath("$.author").value("댓글 작성자"));
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments - 200과 댓글 목록을 반환한다")
    void getComments() throws Exception {
        // given
        given(commentService.getComments(1L)).willReturn(List.of(sampleResponse()));

        // when & then
        mockMvc.perform(get("/api/posts/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("댓글 내용"));
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments - 존재하지 않는 게시글이면 404를 반환한다")
    void getCommentsPostNotFound() throws Exception {
        // given
        given(commentService.getComments(999L)).willThrow(new PostNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/posts/999/comments"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments/{commentId} - 존재하는 ID면 200과 댓글을 반환한다")
    void getComment() throws Exception {
        // given
        given(commentService.getComment(1L, 1L)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/posts/1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("댓글 내용"));
    }

    @Test
    @DisplayName("GET /api/posts/{postId}/comments/{commentId} - 존재하지 않는 ID면 404를 반환한다")
    void getCommentNotFound() throws Exception {
        // given
        given(commentService.getComment(1L, 999L)).willThrow(new CommentNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/posts/1/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("PUT /api/posts/{postId}/comments/{commentId} - 200과 수정된 댓글을 반환한다")
    void updateComment() throws Exception {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest("새 댓글 내용");
        CommentResponse updated = new CommentResponse(1L, 1L, "새 댓글 내용", "댓글 작성자", null, null);
        given(commentService.updateComment(eq(1L), eq(1L), any())).willReturn(updated);

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
        willDoNothing().given(commentService).deleteComment(1L, 1L);

        // when & then
        mockMvc.perform(delete("/api/posts/1/comments/1"))
                .andExpect(status().isNoContent());
    }
}
