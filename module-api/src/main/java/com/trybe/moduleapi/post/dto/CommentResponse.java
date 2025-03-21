package com.trybe.moduleapi.post.dto;

import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.post.entity.Comment;

import java.time.LocalDateTime;

public class CommentResponse {
    public record Detail(
            Long id,
            PostResponse.Summary post,
            UserResponse.Summary writer,
            String content,
            LocalDateTime createdAt
    ){
        public static Detail from(Comment comment){
            return new Detail(comment.getId(), null, UserResponse.Summary.from(comment.getUser()), comment.getContent(), comment.getCreatedAt());
        }
    }

    public record Summary(
            Long id,
            UserResponse.Summary writer,
            String content,
            LocalDateTime createdAt
    ){
        public static Summary from(Comment comment){
            return new Summary(comment.getId(), UserResponse.Summary.from(comment.getUser()), comment.getContent(), comment.getCreatedAt());

        }
    }
}
