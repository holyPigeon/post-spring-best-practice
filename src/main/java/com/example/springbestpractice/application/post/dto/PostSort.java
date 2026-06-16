package com.example.springbestpractice.application.post.dto;

import org.springframework.data.domain.Sort;

public enum PostSort {

    LATEST(Sort.by(Sort.Direction.DESC, "createdAt")),
    OLDEST(Sort.by(Sort.Direction.ASC, "createdAt"));

    private final Sort sort;

    PostSort(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
