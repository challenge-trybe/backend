package com.trybe.moduleapi.challenge.event;

import com.trybe.moduleapi.utils.DateUtils;
import com.trybe.modulecore.challenge.repository.popular.PopularChallengeCache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PopularChallengeEventListener {
    private final PopularChallengeCache popularChallengeCache;

    public PopularChallengeEventListener(PopularChallengeCache popularChallengeCache) {
        this.popularChallengeCache = popularChallengeCache;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeEvent(ChallengeEvent event) {
        Long challengeId = event.getChallenge().getId();

        ChallengeEventType type = event.getType();
        int score = type.getActionType().getPopularityScore();

        if (score == 0) { return; }

        if (type.isScoreUp()) {
            increasePopularity(challengeId, score);
        } else {
            decreasePopularity(challengeId, score);
        }
    }

    private void increasePopularity(Long challengeId, int score) {
        popularChallengeCache.increaseScore(challengeId, score, DateUtils.getToday());
    }

    private void decreasePopularity(Long challengeId, int score) {
        popularChallengeCache.decreaseScore(challengeId, score, DateUtils.getToday());
    }
}
