package com.example.springbestpractice.api.post;

import com.example.springbestpractice.application.post.PostService;
import com.example.springbestpractice.application.post.command.PostDeleteCommand;
import com.example.springbestpractice.application.post.dto.PostCreateRequest;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.application.post.dto.PostUpdateRequest;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.domain.post.PostNotFoundException;
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
import org.springframework.data.domain.Pageable;
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

@WebMvcTest(PostController.class)
@Import({CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("게시글 API")
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PostService postService;

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
    @DisplayName("POST /api/posts - 생성된 게시글을 반환한다")
    void createPost() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest("제목", "내용");
        given(postService.createPost(any())).willReturn(sampleResponse());

        // when & then
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("제목"));
    }

    @Test
    @DisplayName("GET /api/posts - 게시글 페이지를 반환한다")
    void getPosts() throws Exception {
        // given
        PageResponse<PostResponse> page = new PageResponse<>(List.of(sampleResponse()), 0, 20, 1, 1, true, true);
        given(postService.getPosts(any(Pageable.class))).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("제목"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/posts/{id} - 없으면 404를 반환한다")
    void getPostNotFound() throws Exception {
        // given
        given(postService.getPost(999L)).willThrow(new PostNotFoundException(999L));

        // when & then
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/posts/{id} - 수정된 게시글을 반환한다")
    void updatePost() throws Exception {
        // given
        PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용");
        given(postService.updatePost(any())).willReturn(new PostResponse(1L, "새 제목", "새 내용", "작성자", null, null));

        // when & then
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("새 제목"));
    }

    @Test
    @DisplayName("DELETE /api/posts/{id} - 204를 반환한다")
    void deletePost() throws Exception {
        // given
        willDoNothing().given(postService).deletePost(any(PostDeleteCommand.class));

        // when & then
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isNoContent());
    }

    private PostResponse sampleResponse() {
        return new PostResponse(1L, "제목", "내용", "작성자", null, null);
    }
}
