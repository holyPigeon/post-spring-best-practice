package com.example.springbestpractice.domain.post;

import com.example.springbestpractice.common.exception.NotFoundException;

public class PostNotFoundException extends NotFoundException {

    public PostNotFoundException(Long id) {
        super("게시글을 찾을 수 없습니다. id=" + id);
    }
}
