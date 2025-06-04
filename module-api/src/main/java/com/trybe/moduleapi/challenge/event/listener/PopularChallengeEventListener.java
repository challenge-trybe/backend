package com.trybe.moduleapi.challenge.event.listener;

import com.trybe.moduleapi.challenge.event.model.ChallengeActionEvent;
import com.trybe.moduleapi.challenge.event.model.ChallengeParticipationEvent;
import com.trybe.moduleapi.challenge.event.type.ChallengeActionEventType;
import com.trybe.moduleapi.challenge.event.type.ChallengeParticipationEventType;
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
    public void handleChallengeActionEvent(ChallengeActionEvent event) {
        ChallengeActionEventType type = event.getEventType();
        Long challengeId = event.getChallenge().getId();

        int score = type.getScoreType().getPopularityScore();

        if (type.isScoreUp()) {
            increasePopularity(challengeId, score);
        } else {
            decreasePopularity(challengeId, score);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeParticipationEvent(ChallengeParticipationEvent event) {
        ChallengeParticipationEventType type = event.getEventType();
        Long challengeId = event.getChallenge().getId();

        if (type.getScoreType() == null || type.getIsScoreUp() == null) return;

        int score = type.getScoreType().getPopularityScore();

        if (type.getIsScoreUp()) {
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
