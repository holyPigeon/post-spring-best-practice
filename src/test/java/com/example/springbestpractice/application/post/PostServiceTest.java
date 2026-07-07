package com.example.springbestpractice.application.post;

import com.example.springbestpractice.application.post.command.PostCreateCommand;
import com.example.springbestpractice.application.post.command.PostDeleteCommand;
import com.example.springbestpractice.application.post.command.PostUpdateCommand;
import com.example.springbestpractice.application.post.dto.PostCreateRequest;
import com.example.springbestpractice.application.post.dto.PostDetailResponse;
import com.example.springbestpractice.application.post.dto.PostResponse;
import com.example.springbestpractice.application.post.dto.PostUpdateRequest;
import com.example.springbestpractice.common.dto.PageResponse;
import com.example.springbestpractice.common.model.LoginUser;
import com.example.springbestpractice.domain.user.UserRole;
import com.example.springbestpractice.domain.post.Post;
import com.example.springbestpractice.domain.post.PostNotFoundException;
import com.example.springbestpractice.infrastructure.comment.CommentRepository;
import com.example.springbestpractice.infrastructure.post.PostLikeRepository;
import com.example.springbestpractice.infrastructure.post.PostRepository;
import com.example.springbestpractice.support.fixture.PostFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@DisplayName("게시글 서비스")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    CommentRepository commentRepository;

    @Mock
    PostLikeRepository postLikeRepository;

    @InjectMocks
    PostService postService;

    private Post post;
    private LoginUser loginUser;

    @BeforeEach
    void setUp() {
        post = PostFixture.postWithId(1L);
        loginUser = new LoginUser(1L, "writer@test.com", "작성자", UserRole.USER);
    }

    @Test
    @DisplayName("정상 입력이면 게시글을 저장하고 응답을 반환한다")
    void createPost() {
        // given
        PostCreateRequest request = new PostCreateRequest("제목", "내용");
        given(postRepository.save(any(Post.class))).willReturn(post);

        // when
        postService.createPost(PostCreateCommand.from(request, loginUser));

        // then
        ArgumentCaptor<Post> savedPost = ArgumentCaptor.forClass(Post.class);
        verify(postRepository).save(savedPost.capture());
        assertThat(savedPost.getValue())
                .extracting("title", "content", "authorId", "authorNickname")
                .containsExactly("제목", "내용", 1L, "작성자");
    }

    @Nested
    @DisplayName("게시글 조회")
    class Find {

        @Test
        @DisplayName("존재하지 않는 ID면 예외를 던진다")
        void throwExceptionWhenNotFound() {
            // given
            given(postRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> postService.getPost(999L, loginUser))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("존재하는 ID면 응답을 반환한다")
        void returnPostResponse() {
            // given
            given(postRepository.findById(1L)).willReturn(Optional.of(post));
            given(postLikeRepository.existsByPostIdAndUserId(1L, loginUser.id())).willReturn(true);

            // when
            PostDetailResponse result = postService.getPost(1L, loginUser);

            // then
            assertThat(result.title()).isEqualTo("title");
            assertThat(result.liked()).isTrue();
        }
    }

    @Test
    @DisplayName("페이지 요청이면 게시글 페이지를 반환한다")
    void returnPostPage() {
        // given
        Post another = PostFixture.postWithId(2L, "제목2", "내용2", 2L, "다른작성자");
        Pageable pageable = PageRequest.of(0, 20);
        given(postRepository.findAll(pageable))
                .willReturn(new PageImpl<>(List.of(post, another), pageable, 2));

        // when
        PageResponse<PostResponse> result = postService.getPosts(pageable);

        // then
        assertThat(result.content()).hasSize(2)
                .extracting("title")
                .containsExactly("title", "제목2");
        assertThat(result.content())
                .extracting("likeCount")
                .containsExactly(0L, 0L);
        assertThat(result.totalElements()).isEqualTo(2);
    }

    @Nested
    @DisplayName("게시글 수정")
    class Update {

        @Test
        @DisplayName("소유자면 제목과 내용을 수정한다")
        void updatePost() {
            // given
            PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용");
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when
            PostResponse result = postService.updatePost(PostUpdateCommand.from(1L, request, loginUser));

            // then
            assertThat(result)
                    .extracting("title", "content")
                    .containsExactly("새 제목", "새 내용");
        }

        @Test
        @DisplayName("소유자가 아니면 예외를 던진다")
        void throwExceptionWhenNotOwner() {
            // given
            PostUpdateRequest request = new PostUpdateRequest("새 제목", "새 내용");
            LoginUser otherUser = new LoginUser(2L, "other@test.com", "다른사용자", UserRole.USER);
            given(postRepository.findById(1L)).willReturn(Optional.of(post));

            // when & then
            assertThatThrownBy(() -> postService.updatePost(PostUpdateCommand.from(1L, request, otherUser)))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    @DisplayName("소유자면 댓글과 게시글을 삭제한다")
    void deletePost() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(PostDeleteCommand.from(1L, loginUser));

        // then
        verify(postLikeRepository).deleteByPostId(1L);
        verify(commentRepository).deleteByPostId(1L);
        verify(postRepository).delete(post);
    }
}
