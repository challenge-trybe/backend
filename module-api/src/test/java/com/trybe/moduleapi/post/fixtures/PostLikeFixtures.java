package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.post.dto.PostResponse;

import java.util.Set;

public class PostLikeFixtures {
    public static final Set<Long> 게시글_좋아요_누른_유저목록 = Set.of(1L, 2L, 3L);

    public static final PostResponse.Like 좋아요_추가_응답 = PostResponse.Like.from(5, true);
    public static final PostResponse.Like 좋아요_삭제_응답 = PostResponse.Like.from(4, false);
    public static final int 좋아요_개수 = 10;
    public static final Long 좋아요_개수L = 20L;

    private static final String POST_LIKE_SUFFIX = ":liked";
    private static final String USER_REDIS_KEY = "user:%d" + POST_LIKE_SUFFIX + ":posts";
    private static final String POST_REDIS_KEY = "post:%d" + POST_LIKE_SUFFIX + ":users";
    private static final String POST_LIKE_DELETE_KEY = POST_REDIS_KEY + ":deleted";
    public static String createUserKey(Long userId) {
        return String.format(USER_REDIS_KEY, userId);
    }

    public static String createDeletedKey(Long postId) {
        return String.format(POST_LIKE_DELETE_KEY, postId);
    }

    public static String createPostKey(Long postId) {
        return String.format(POST_REDIS_KEY, postId);
    }
}
