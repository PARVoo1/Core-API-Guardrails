package com.example.core.api.guardrails.dto;

import com.example.core.api.guardrails.entity.AuthorType;
import lombok.Data;

@Data
public class CommentDto {
    private Long postId;
    private Long parentCommentId;
    private Long authorId;
    private AuthorType authorType;
    private String content;

}
