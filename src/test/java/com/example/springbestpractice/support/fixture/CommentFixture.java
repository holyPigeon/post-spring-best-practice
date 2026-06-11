package com.example.springbestpractice.support.fixture;

import com.example.springbestpractice.domain.comment.Comment;
import com.example.springbestpractice.domain.post.Post;
import org.springframework.test.util.ReflectionTestUtils;

public final class CommentFixture {

    private CommentFixture() {
    }

    public static Comment commentWithId(Long id, Post post) {
        return commentWithId(id, post, "comment", 2L, "commenter");
    }

    public static Comment commentWithId(
            Long id,
            Post post,
            String content,
            Long authorId,
            String authorNickname
    ) {
        Comment comment = Comment.create(post, content, authorId, authorNickname);
        ReflectionTestUtils.setField(comment, "id", id);
        return comment;
    }
}
