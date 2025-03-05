package com.trybe.moduleapi.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse{
    public record Detail(
            Long id,
            String title,
            String content,
            PostCategory category,
            UserResponse.Summary user,
            LocalDateTime createdAt,
            List<ChallengeResponse.Summary> challengeSummary
    ){
        public static Detail from(Post post, List<ChallengeResponse.Summary> summaryChallenge){
            return new Detail(post.getId(),
                              post.getTitle(),
                              post.getContent(),
                              post.getCategory(),
                              com.trybe.moduleapi.user.dto.response.UserResponse.Summary.from(post.getUser()),
                              post.getCreatedAt(),
                              summaryChallenge);
        }
    }
    public record Summary(
            Long id,
            String title,
            String content,
            PostCategory category,
            UserResponse.Summary user,
            @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // JSON 직렬화 시 포맷 적용
            LocalDateTime createdAt
    ){
        public static Summary from(Post post){
            return new Summary(post.getId(),
                               post.getTitle(),
                               post.getContent(),
                               post.getCategory(),
                               UserResponse.Summary.from(post.getUser()),
                               post.getCreatedAt());
        }
    }
}
