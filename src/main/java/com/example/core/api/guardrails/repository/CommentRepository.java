package com.example.core.api.guardrails.repository;

import com.example.core.api.guardrails.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {
}
