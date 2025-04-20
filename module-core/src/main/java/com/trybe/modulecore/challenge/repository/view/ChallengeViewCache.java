package com.trybe.modulecore.challenge.repository.view;

public interface ChallengeViewCache {
    boolean hasViewed(Long userId, Long challengeId);
    void recordView(Long userId, Long challengeId);
}
