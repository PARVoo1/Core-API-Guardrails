package com.example.core.api.guardrails.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false)
    private AuthorType authorType;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
}
