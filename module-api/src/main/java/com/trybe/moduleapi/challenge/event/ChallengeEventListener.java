package com.trybe.moduleapi.challenge.event;

import com.trybe.moduleapi.utils.DateUtils;
import com.trybe.modulecore.challenge.repository.popular.PopularChallengeCache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChallengeEventListener {
    private final PopularChallengeCache popularChallengeCache;

    public ChallengeEventListener(PopularChallengeCache popularChallengeCache) {
        this.popularChallengeCache = popularChallengeCache;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeEvent(ChallengeEvent event) {
        Long challengeId = event.getChallengeId();
        ChallengeEventType type = event.getType();
        int score = type.getActionType().getScore();

        if (type.isScoreUp()) {
            increaseScore(challengeId, score);
        } else {
            decreaseScore(challengeId, score);
        }
    }

    private void increaseScore(Long challengeId, int amount) {
        popularChallengeCache.increaseScore(challengeId, amount, DateUtils.getToday());
    }

    private void decreaseScore(Long challengeId, int amount) {
        popularChallengeCache.decreaseScore(challengeId, amount, DateUtils.getToday());
    }
}
