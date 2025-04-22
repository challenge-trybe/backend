package com.trybe.moduleapi.challenge.event;

import com.trybe.moduleapi.utils.DateUtils;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.popular.PopularChallengeCache;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChallengeEventListener {
    private final PopularChallengeCache popularChallengeCache;
    private final ChallengePreferenceCache challengePreferenceCache;

    public ChallengeEventListener(PopularChallengeCache popularChallengeCache, ChallengePreferenceCache challengePreferenceCache) {
        this.popularChallengeCache = popularChallengeCache;
        this.challengePreferenceCache = challengePreferenceCache;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeEvent(ChallengeEvent event) {
        Challenge challenge = event.getChallenge();

        Long challengeId = challenge.getId();
        Long userId = event.getUserId();

        ChallengeEventType type = event.getType();
        int score = type.getActionType().getScore();

        if (type.getActionType() == ChallengeActionType.CREATE) {
            increasePreference(userId, challenge, score);
            return;
        }

        if (type.isScoreUp()) {
            increasePopularity(challengeId, score);
            increasePreference(userId, challenge, score);
        } else {
            decreasePopularity(challengeId, score);
            decreasePreference(userId, challenge, score);
        }
    }

    private void increasePopularity(Long challengeId, int score) {
        popularChallengeCache.increaseScore(challengeId, score, DateUtils.getToday());
    }

    private void decreasePopularity(Long challengeId, int score) {
        popularChallengeCache.decreaseScore(challengeId, score, DateUtils.getToday());
    }

    private void increasePreference(Long userId, Challenge challenge, int score) {
        challengePreferenceCache.addPreference(userId, challenge, score);
    }

    private void decreasePreference(Long userId, Challenge challenge, int score) {
        challengePreferenceCache.removePreference(userId, challenge, score);
    }
}
