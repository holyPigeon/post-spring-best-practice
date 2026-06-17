package com.example.springbestpractice.support.fixture;

import com.example.springbestpractice.domain.post.Post;
import org.springframework.test.util.ReflectionTestUtils;

public final class PostFixture {

    private PostFixture() {
    }

    public static Post post() {
        return Post.create("title", "content", 1L, "writer");
    }

    public static Post postWithId(Long id) {
        Post post = post();
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    public static Post postWithLikeCount(Long id, long likeCount) {
        Post post = postWithId(id);
        ReflectionTestUtils.setField(post, "likeCount", likeCount);
        return post;
    }

    public static Post postWithId(Long id, String title, String content, Long authorId, String authorNickname) {
        Post post = Post.create(title, content, authorId, authorNickname);
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }
}
