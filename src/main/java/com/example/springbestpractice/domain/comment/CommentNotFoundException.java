package com.example.springbestpractice.domain.comment;

import com.example.springbestpractice.common.exception.NotFoundException;

public class CommentNotFoundException extends NotFoundException {

    public CommentNotFoundException(Long id) {
        super("댓글을 찾을 수 없습니다. id=" + id);
    }
}
