package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.service.ChallengeBookmarkService;
import com.trybe.moduleapi.common.dto.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges/bookmarks")
public class ChallengeBookmarkController {
    private final ChallengeBookmarkService challengeBookmarkService;

    public ChallengeBookmarkController(ChallengeBookmarkService challengeBookmarkService) {
        this.challengeBookmarkService = challengeBookmarkService;
    }

    @PostMapping("/{challengeId}")
    public ChallengeResponse.Bookmark addBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("challengeId") Long challengeId
    ) {
        return challengeBookmarkService.addBookmark(userDetails.getUser(), challengeId);
    }

    @DeleteMapping("/{challengeId}")
    public ChallengeResponse.Bookmark removeBookmark(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("challengeId") Long challengeId
    ) {
        return challengeBookmarkService.removeBookmark(userDetails.getUser(), challengeId);
    }

    @GetMapping("/my")
    public PageResponse<ChallengeResponse.Summary> getMyBookmarkedChallenges(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ) {
        return challengeBookmarkService.getMyBookmarkedChallenges(userDetails.getUser(), pageable);
    }
}