package com.example.springbestpractice.application.post.dto;

import org.springframework.data.domain.Sort;

public enum PostSort {

    LATEST(Sort.by(
            Sort.Order.desc("createdAt"),
            Sort.Order.desc("id")
    )),
    OLDEST(Sort.by(
            Sort.Order.asc("createdAt"),
            Sort.Order.asc("id")
    ));

    private final Sort sort;

    PostSort(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
