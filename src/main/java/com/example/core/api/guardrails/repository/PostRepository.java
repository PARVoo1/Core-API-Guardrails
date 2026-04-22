package com.example.core.api.guardrails.repository;

import com.example.core.api.guardrails.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post,Long>{
}
