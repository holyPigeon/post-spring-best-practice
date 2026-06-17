package com.example.springbestpractice.api.post;

import com.example.springbestpractice.application.post.PostLikeService;
import com.example.springbestpractice.application.post.dto.PostLikeResponse;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.security.CustomUserDetails;
import com.example.springbestpractice.infrastructure.security.CurrentUserArgumentResolver;
import com.example.springbestpractice.infrastructure.security.SecurityWebMvcConfig;
import com.example.springbestpractice.support.fixture.UserFixture;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostLikeController.class)
@Import({CurrentUserArgumentResolver.class, SecurityWebMvcConfig.class})
@DisplayName("Post like API")
class PostLikeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PostLikeService postLikeService;

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
    @DisplayName("POST /api/posts/{postId}/likes returns liked state")
    void like() throws Exception {
        // given
        given(postLikeService.like(any())).willReturn(new PostLikeResponse(1L, 1, true));

        // when & then
        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.liked").value(true));
    }

    @Test
    @DisplayName("DELETE /api/posts/{postId}/likes returns unliked state")
    void unlike() throws Exception {
        // given
        given(postLikeService.unlike(any())).willReturn(new PostLikeResponse(1L, 0, false));

        // when & then
        mockMvc.perform(delete("/api/posts/1/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(1))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.liked").value(false));
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/likes returns 404 when post does not exist")
    void likePostNotFound() throws Exception {
        // given
        given(postLikeService.like(any())).willThrow(new PostNotFoundException(999L));

        // when & then
        mockMvc.perform(post("/api/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/posts/{postId}/likes returns 409 on concurrent duplicate like")
    void likeConflict() throws Exception {
        // given
        given(postLikeService.like(any())).willThrow(new DataIntegrityViolationException("duplicate"));

        // when & then
        mockMvc.perform(post("/api/posts/1/likes"))
                .andExpect(status().isConflict());
    }
}
