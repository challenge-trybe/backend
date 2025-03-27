package com.trybe.moduleapi.challenge.fixtures;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChallengeBookmarkFixtures {
    public static final int 초기_북마크_수 = 0;
    public static final int 북마크_수 = 5;
    public static final Boolean 북마크_여부_참 = true;
    public static final Boolean 북마크_여부_거짓 = false;

    public static final Set<Long> 북마크된_챌린지_ID_목록 = new LinkedHashSet<>(List.of(2L, 3L, 1L));
    public static final Set<Long> 빈_북마크된_챌린지_ID_목록 = new LinkedHashSet<>();

    /* Response DTO */
    public static final ChallengeResponse.Bookmark 초기_북마크_응답 = new ChallengeResponse.Bookmark(
            초기_북마크_수,
            false
    );

    public static final ChallengeResponse.Bookmark 북마크_참_응답 = new ChallengeResponse.Bookmark(
            북마크_수,
            북마크_여부_참
    );

    public static final ChallengeResponse.Bookmark 북마크_거짓_응답 = new ChallengeResponse.Bookmark(
            북마크_수,
            북마크_여부_거짓
    );
}
