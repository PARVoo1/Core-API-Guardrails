package com.example.core.api.guardrails.dto;

import com.example.core.api.guardrails.entity.AuthorType;
import lombok.Data;

@Data
public class LikeDto {

    private Long authorId;
    private AuthorType authorType;

}
