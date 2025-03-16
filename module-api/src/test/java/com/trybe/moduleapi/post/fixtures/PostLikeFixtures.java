package com.trybe.moduleapi.post.fixtures;

import com.trybe.moduleapi.post.dto.PostResponse;

import java.util.Set;

public class PostLikeFixtures {
    public static final Set<Long> 게시글_좋아요_누른_유저목록 = Set.of(1L, 2L, 3L);

    public static PostResponse.Like 좋아요_추가_응답 = PostResponse.Like.from(5, true);
    public static PostResponse.Like 좋아요_삭제_응답 = PostResponse.Like.from(4, false);
}
