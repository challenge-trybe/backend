package com.trybe.moduleapi.post.dto;

import com.trybe.modulecore.post.entity.Comment;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {
    private static final String COMMENT_VALIDATION_MESSAGE = "댓글을 입력해주세요.";
    private static final int COMMENT_MAX = 500;
    private static final String COMMENT_MESSAGE_SIZE_VALIDATION_MESSAGE = "댓글은 500자 이상 넘길 수 없습니다.";

    public record Enroll(
            @NotBlank(message = COMMENT_VALIDATION_MESSAGE)
            @Size(max = COMMENT_MAX, message = COMMENT_MESSAGE_SIZE_VALIDATION_MESSAGE)
            String content
    ){
        public Comment toEntity(User user, Post post, String content){
            return Comment.builder()
                          .user(user)
                          .post(post)
                          .content(content)
                          .build();
        }
    }

    public record Update(
            @NotBlank(message = COMMENT_VALIDATION_MESSAGE)
            @Size(max = COMMENT_MAX, message = COMMENT_MESSAGE_SIZE_VALIDATION_MESSAGE)
            String content
    ){}
}
