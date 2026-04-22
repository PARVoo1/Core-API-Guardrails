package com.example.core.api.guardrails.entity;


import jakarta.persistence.*;
import lombok.Data;


import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "post_id")
    private Long postId;
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "author_type",nullable = false)
    private AuthorType authorType;
    @Column(columnDefinition = "TEXT")
    private String content;
    @Column(name = "depth_level",nullable = false)
    private int depthLevel;
    @Column(name = "parent_comment_id")
    private Long parentCommentId;
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt=LocalDateTime.now();
}
