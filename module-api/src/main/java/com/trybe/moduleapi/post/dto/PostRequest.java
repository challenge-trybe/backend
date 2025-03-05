package com.trybe.moduleapi.post.dto;

import com.trybe.moduleapi.annotation.NotEmptyList;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.enums.PostCategory;
import com.trybe.modulecore.post.enums.PostOrder;
import com.trybe.modulecore.user.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public class PostRequest {
    private static final String CATEGORIES_NOT_EMPTY_MESSAGE = "포스트 카테고리를 선택해주세요.";
    private static final String TITLE_VALIDATION_MESSAGE = "제목을 입력해주세요.";
    private static final int TITLE_MESSAGE_MIN = 10;
    private static final int TITLE_MESSAGE_MAX = 20;
    private static final String TITLE_MESSAGE_SIZE_VALIDATION_MESSAGE = "내용은 10자~20자 사이로 입력해주세요.";

    private static final String CONTENT_VALIDATION_MESSAGE = "내용을 입력해주세요.";
    private static final int CONTENT_MESSAGE_MIN = 10;
    private static final int CONTENT_MESSAGE_MAX = 3000;
    private static final String CONTENT_MESSAGE_SIZE_VALIDATION_MESSAGE = "내용은 10자~3000자 사이로 입력해주세요.";

    public record Create(
            @NotBlank(message = TITLE_VALIDATION_MESSAGE)
            @Size(min = TITLE_MESSAGE_MIN, max = TITLE_MESSAGE_MAX, message = TITLE_MESSAGE_SIZE_VALIDATION_MESSAGE)
            String title,

            @NotBlank(message = CONTENT_VALIDATION_MESSAGE)
            @Size(min = CONTENT_MESSAGE_MIN, max = CONTENT_MESSAGE_MAX, message = CONTENT_MESSAGE_SIZE_VALIDATION_MESSAGE)
            String content,

            @NotNull(message = CATEGORIES_NOT_EMPTY_MESSAGE)
            PostCategory category,

            Set<Long> challengeId
    ){
        public Post toEntity(User user) {
            return Post.builder()
                       .title(title)
                       .content(content)
                       .user(user)
                       .category(category)
                       .build();

        }
    }

    public record Update(
            @NotBlank(message = TITLE_VALIDATION_MESSAGE)
            @Size(min = TITLE_MESSAGE_MIN, max = TITLE_MESSAGE_MAX, message = TITLE_MESSAGE_SIZE_VALIDATION_MESSAGE)
            String title,

            @NotBlank(message = CONTENT_VALIDATION_MESSAGE)
            @Size(min = CONTENT_MESSAGE_MIN, max = CONTENT_MESSAGE_MAX, message = CONTENT_MESSAGE_SIZE_VALIDATION_MESSAGE)
            String content,

            @NotNull(message = CATEGORIES_NOT_EMPTY_MESSAGE)
            PostCategory category,

            Set<Long> challengeId
    ){}

    public record Read(
            String keyword,

            @NotEmptyList(message = CATEGORIES_NOT_EMPTY_MESSAGE)
            List<PostCategory> categories,

            PostOrder order
    ){}
}
