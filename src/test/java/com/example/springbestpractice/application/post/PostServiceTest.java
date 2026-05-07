package com.example.springbestpractice.application.post;

import com.example.springbestpractice.api.post.dto.PostCreateRequest;
import com.example.springbestpractice.api.post.dto.PostResponse;
import com.example.springbestpractice.api.post.dto.PostUpdateRequest;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("포스트 서비스")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PostService postService;

    private Post post;

    @BeforeEach
    void setUp() {
        post = Post.builder()
                .id(1L)
                .title("제목")
                .content("내용")
                .author("작성자")
                .build();
    }

    @Nested
    @DisplayName("게시글 생성")
    class Create {

        @Test
        @DisplayName("정상 입력이면 게시글을 저장하고 PostResponse를 반환한다")
        void createPost() {
            // given
            PostCreateRequest request = new PostCreateRequest("제목", "내용", "작성자");
            given(postRepository.save(any(Post.class))).willReturn(post);

            // when
            PostResponse result = postService.createPost(request);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("제목");
            assertThat(result.content()).isEqualTo("내용");
            assertThat(result.author()).isEqualTo("작성자");
            verify(postRepository).save(any(Post.class));
        }
    }

    @Nested
    @DisplayName("게시글 단건 조회")
    class Find {

        @Test
        @DisplayName("존재하지 않는 ID면 PostNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPost(999L))
                    .isInstanceOf(PostNotFoundException.class)
                    .hasMessage("게시글을 찾을 수 없습니다. id=999");
        }

        @Test
        @DisplayName("존재하는 ID면 PostResponse를 반환한다")
        void returnPostResponse() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            PostResponse result = postService.getPost(1L);

            // then
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.title()).isEqualTo("제목");
        }
    }

    @Nested
    @DisplayName("게시글 전체 조회")
    class FindAll {

        @Test
        @DisplayName("게시글이 없으면 빈 리스트를 반환한다")
        void returnEmptyList() {
            // given
            given(postRepository.findAll()).willReturn(List.of());

            // when
            List<PostResponse> result = postService.getAllPosts();

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("게시글이 있으면 전체 목록을 반환한다")
        void returnAllPosts() {
            // given
            Post another = Post.builder().id(2L).title("제목2").content("내용2").author("작성자2").build();
            given(postRepository.findAll()).willReturn(List.of(post, another));

            // when
            List<PostResponse> result = postService.getAllPosts();

            // then
            assertThat(result).hasSize(2)
                    .extracting("title")
                    .containsExactly("제목", "제목2");
        }
    }

    @Nested
    @DisplayName("게시글 수정")
    class Update {

        @Test
        @DisplayName("존재하지 않는 ID면 PostNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용");
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.updatePost(999L, request))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 제목과 내용을 수정하고 반환한다")
        void updatePost() {
            // given
            PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용");
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            PostResponse result = postService.updatePost(1L, request);

            // then
            assertThat(result.title()).isEqualTo("새 제목");
            assertThat(result.content()).isEqualTo("새 내용");
        }
    }

    @Nested
    @DisplayName("게시글 삭제")
    class Delete {

        @Test
        @DisplayName("존재하지 않는 ID면 PostNotFoundException을 던진다")
        void throwExceptionWhenNotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.deletePost(999L))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 게시글을 삭제한다")
        void deletePost() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            postService.deletePost(1L);

            // then
            verify(postRepository).delete(post);
        }
    }
}
