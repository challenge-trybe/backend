package com.trybe.modulecore.challenge.repository.bookmark;

import java.util.Set;

public interface ChallengeBookmarkCache {
    void addBookmark(Long userId, Long challengeId);
    void removeBookmark(Long userId, Long challengeId);
    boolean isBookmarked(Long userId, Long challengeId);
    int getBookmarkCount(Long challengeId);
    int getUserBookmarkCount(Long userId);
    Set<Long> getBookmarkedChallenges(Long userId, int start, int end);
    void removeBookmarksByChallenge(Long challengeId);
}
