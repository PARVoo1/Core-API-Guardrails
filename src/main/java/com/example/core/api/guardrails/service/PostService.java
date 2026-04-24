package com.example.core.api.guardrails.service;

import com.example.core.api.guardrails.dto.CommentDto;
import com.example.core.api.guardrails.dto.LikeDto;
import com.example.core.api.guardrails.dto.PostDto;
import com.example.core.api.guardrails.entity.AuthorType;
import com.example.core.api.guardrails.entity.Comment;
import com.example.core.api.guardrails.entity.Post;
import com.example.core.api.guardrails.repository.CommentRepository;
import com.example.core.api.guardrails.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String POST_KEY ="post:";

    public Post createPost(PostDto post) {
        Post newPost = new Post();
        newPost.setAuthorId(post.getAuthorId());
        newPost.setAuthorType(post.getAuthorType());
        newPost.setContent(post.getContent());

        return postRepository.save(newPost);
    }
    @Transactional
    public Comment createComment(CommentDto comment) {
        Post post=postRepository.findById(comment.getPostId())
                .orElseThrow(()->new RuntimeException("Post not found with id: "+comment.getPostId()));
        int calculatedDepth=1;
        Comment parentComment;
        Long targetAuthorId=post.getAuthorId();
        AuthorType targetAuthorType=post.getAuthorType();
        if(comment.getParentCommentId()!=null){
            parentComment = commentRepository.findById(comment.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found with id: "));
            calculatedDepth=parentComment.getDepthLevel()+1;
            targetAuthorId=parentComment.getAuthorId();
            targetAuthorType=parentComment.getAuthorType();
        }

        if(comment.getAuthorType()== AuthorType.BOT){
            if (targetAuthorType==AuthorType.USER){
                String coolDownKey="cooldown:bot_"+comment.getAuthorId()+":human_"+targetAuthorId;
                if(Boolean.TRUE.equals(redisTemplate.hasKey(coolDownKey)) ){
                    throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,"Bot is on cooldown");
                }
            }
            if(calculatedDepth>20){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid depth level");
            }
            String botCountKey = POST_KEY + comment.getPostId() + ":bot_count";
            Long botCount = redisTemplate.opsForValue().increment(botCountKey);
            if(botCount!=null&&botCount>100){
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Bot comment limit exceeded");
            }
        }
        Comment newComment = new Comment();
        newComment.setPostId(comment.getPostId());
        newComment.setAuthorId(comment.getAuthorId());
        newComment.setAuthorType(comment.getAuthorType());
        newComment.setContent(comment.getContent());
        newComment.setParentCommentId(comment.getParentCommentId());
        newComment.setDepthLevel(calculatedDepth);
        Comment savedComment = commentRepository.save(newComment);



        String viralityKey = POST_KEY + newComment.getPostId() + ":virality";

        long points = (savedComment.getAuthorType() == AuthorType.BOT) ? 1 : 50;

        redisTemplate.opsForValue().increment(viralityKey, points);

        if(targetAuthorType==AuthorType.USER&&savedComment.getAuthorType()==AuthorType.BOT){
            String coolDownKey = "cooldown:bot_" +savedComment.getAuthorId()+ ":human_"+targetAuthorId;
            redisTemplate.opsForValue().set(coolDownKey,"locked",10,TimeUnit.MINUTES);
            handleNotification(targetAuthorId,savedComment.getAuthorId());
        }

        return savedComment;
    }

    public void likePost(Long postId, LikeDto like) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isEmpty()) {
          throw new RuntimeException("Post not found with id: " + postId);
        }
        String viralityKey= POST_KEY +postId+":virality";
        long viralityPoints=0;
        if(like.getAuthorType()==AuthorType.USER){
            viralityPoints=20;
        }
        if(viralityPoints>0){
            redisTemplate.opsForValue().increment(viralityKey,viralityPoints);
        }

    }
    private void handleNotification(Long userId,Long botId){
        String coolDownKey="user:"+userId+":cooldown";
        String pendingNotifs="user:"+userId+":pending";
        String notificationMessage="Bot:"+botId+" replied to your post";
        Boolean acquiredLock=redisTemplate.opsForValue().setIfAbsent(coolDownKey, "locked", 15, TimeUnit.MINUTES);
        if(Boolean.TRUE.equals(acquiredLock)){
            log.info("Push Notification Sent to User: {}", notificationMessage);
        } else {
            redisTemplate.opsForList().rightPush(pendingNotifs, notificationMessage);
        }


    }

}
