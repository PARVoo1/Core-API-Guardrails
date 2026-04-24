package com.example.core.api.guardrails.controller;

import com.example.core.api.guardrails.dto.CommentDto;
import com.example.core.api.guardrails.dto.LikeDto;
import com.example.core.api.guardrails.dto.PostDto;
import com.example.core.api.guardrails.entity.Comment;
import com.example.core.api.guardrails.entity.Post;
import com.example.core.api.guardrails.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody PostDto post) {
        try {
            Post savedPost = postService.createPost(post);
            return ResponseEntity.ok(savedPost);
        }catch(Exception e){
            log.error("Exception occurred while trying to save post",e);
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> createComment(@PathVariable Long postId, @RequestBody CommentDto comment) {
        try {
            comment.setPostId(postId);
            Comment savedComment = postService.createComment(comment);
            return ResponseEntity.ok(savedComment);
        }catch(Exception e){
            log.error("Exception occurred while trying to save comment",e);
        }
        return ResponseEntity.badRequest().build();
    }
    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long postId, @RequestBody LikeDto like) {
        try {
            postService.likePost(postId, like);
            return ResponseEntity.ok("Post liked successfully");
        }catch(Exception e){
            log.error("Exception occurred while trying to like post",e);
            return ResponseEntity.badRequest().build();
        }


    }
}
