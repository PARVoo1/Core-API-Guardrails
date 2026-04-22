package com.example.core.api.guardrails.service;

import com.example.core.api.guardrails.entity.Comment;
import com.example.core.api.guardrails.entity.Post;
import com.example.core.api.guardrails.repository.CommentRepository;
import com.example.core.api.guardrails.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Post createPost(Post post) {
        Post newPost = new Post();
        newPost.setAuthorId(post.getAuthorId());
        newPost.setAuthorType(post.getAuthorType());
        newPost.setContent(post.getContent());

        return postRepository.save(newPost);
    }
    public Comment createComment(Comment comment) {
        Optional<Post> isPresent = postRepository.findById(comment.getPostId());
        if (isPresent.isPresent()) {
            Comment newComment = new Comment();
            newComment.setPostId(comment.getPostId());
            newComment.setAuthorId(comment.getAuthorId());
            newComment.setAuthorType(comment.getAuthorType());
            newComment.setContent(comment.getContent());
            newComment.setDepthLevel(1);
            return commentRepository.save(newComment);

        }
        throw new RuntimeException("Post not found with id: " + comment.getPostId());
    }

}
