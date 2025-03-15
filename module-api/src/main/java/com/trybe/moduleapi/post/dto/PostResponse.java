package com.trybe.moduleapi.post.dto;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostResponse{
    public record Detail(
            Long id,
            String title,
            String content,
            PostCategory category,
            UserResponse.Summary writer,
            LocalDateTime createdAt,
            int likes,
            List<ChallengeResponse.Summary> challenges
    ){
        public static Detail from(Post post, List<Challenge> challenges, int likes){
            return new Detail(post.getId(),
                              post.getTitle(),
                              post.getContent(),
                              post.getCategory(),
                              UserResponse.Summary.from(post.getUser()),
                              post.getCreatedAt(),
                              likes,
                              challenges.stream().map(ChallengeResponse.Summary::from).collect(Collectors.toList()));
        }
    }
    public record Summary(
            Long id,
            String title,
            PostCategory category,
            UserResponse.Summary writer,
            LocalDateTime createdAt
    ){
        public static Summary from(Post post){
            return new Summary(post.getId(),
                               post.getTitle(),
                               post.getCategory(),
                               UserResponse.Summary.from(post.getUser()),
                               post.getCreatedAt());
        }
    }

    public record Like(
            int totalCount,
            boolean isLiked
    ){
        public static Like from(int totalCount, boolean isLiked) {
            return new Like(totalCount, isLiked);
        }

    }
}
