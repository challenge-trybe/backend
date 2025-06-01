package com.trybe.moduleapi.challenge.event.listener;

import com.trybe.moduleapi.challenge.event.model.ChallengeEvent;
import com.trybe.moduleapi.challenge.event.ChallengeEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ChallengePreferenceEventListener {
    private final ChallengePreferenceCache challengePreferenceCache;

    public ChallengePreferenceEventListener(ChallengePreferenceCache challengePreferenceCache) {
        this.challengePreferenceCache = challengePreferenceCache;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeEvent(ChallengeEvent event) {
        Challenge challenge = event.getChallenge();
        Long userId = event.getUserId();

        ChallengeEventType type = event.getType();
        int score = event.getType().getActionType().getPreferenceScore();

        if (score == 0) { return; }

        if (type.isScoreUp()) {
            challengePreferenceCache.addPreference(userId, challenge, score);
        } else {
            challengePreferenceCache.removePreference(userId, challenge, score);
        }
    }
}