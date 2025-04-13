package com.trybe.modulecore.challenge.repository.popular;

import java.time.LocalDate;
import java.util.List;

public interface PopularChallengeCache {
    void increaseScore(Long challengeId, int score, LocalDate date);
    void decreaseScore(Long challengeId, int score, LocalDate date);
    List<Long> getTopPopularChallenges(LocalDate date, int count);
}
