package com.trybe.modulecore.challenge.repository.popular;

import java.time.LocalDate;
import java.util.Set;

public interface PopularChallengeCache {
    void increaseScore(Long challengeId, int score, LocalDate date);
    void decreaseScore(Long challengeId, int score, LocalDate date);
    Set<Long> getTopPopularChallenges(LocalDate date, int count);
}
