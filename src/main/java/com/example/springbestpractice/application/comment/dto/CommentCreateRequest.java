package com.example.springbestpractice.application.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank(message = "댓글 내용은 필수입니다.")
        @Size(max = 1000, message = "댓글 내용은 1000자 이하여야 합니다.")
        String content,

        @NotBlank(message = "작성자는 필수입니다.")
        @Size(max = 20, message = "작성자는 20자 이하여야 합니다.")
        String author
) {
}
