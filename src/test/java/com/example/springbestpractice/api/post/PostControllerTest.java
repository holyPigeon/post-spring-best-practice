package com.example.springbestpractice.api.post;

import com.example.springbestpractice.application.post.PostService;
import com.example.springbestpractice.application.post.dto.PostCreateRequest;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.application.post.dto.PostUpdateRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@DisplayName("게시글 API")
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PostService postService;

    private PostResponse sampleResponse() {
        return new PostResponse(1L, "제목", "내용", "작성자", null, null);
    }

    @Test
    @DisplayName("POST /api/posts - 201과 생성된 게시글을 반환한다")
    void createPost() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest("제목", "내용", "작성자");
        given(postService.createPost(any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("제목"))
                .andExpect(jsonPath("$.author").value("작성자"));
    }

    @Test
    @DisplayName("POST /api/posts - 제목이 비어 있으면 400을 반환한다")
    void createPostBlankTitle() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(" ", "내용", "작성자");

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("제목은 필수입니다."));
    }

    @Test
    @DisplayName("GET /api/posts - 200과 게시글 목록을 반환한다")
    void getAllPosts() throws Exception {
        // given
        given(postService.getAllPosts()).willReturn(List.of(sampleResponse()));

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("제목"));
    }

    @Test
    @DisplayName("GET /api/posts/{id} - 존재하는 ID면 200과 게시글을 반환한다")
    void getPost() throws Exception {
        // given
        given(postService.getPost(1L)).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("제목"));
    }

    @Test
    @DisplayName("GET /api/posts/{id} - 존재하지 않는 ID면 404를 반환한다")
    void getPostNotFound() throws Exception {
        // given
        given(postService.getPost(999L)).willThrow(new PostNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다. id=999"));
    }

    @Test
    @DisplayName("PUT /api/posts/{id} - 200과 수정된 게시글을 반환한다")
    void updatePost() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용");
        PostResponse updated = new PostResponse(1L, "새 제목", "새 내용", "작성자", null, null);
        given(postService.updatePost(eq(1L), any())).willReturn(updated);

        // when & then
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("새 제목"))
                .andExpect(jsonPath("$.content").value("새 내용"));
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - 204를 반환한다")
    void deletePost() throws Exception {
        // given
        willDoNothing().given(postService).deletePost(1L);

        // when & then
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());
    }
}
