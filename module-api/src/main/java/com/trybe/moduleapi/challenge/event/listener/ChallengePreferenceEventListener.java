package com.trybe.moduleapi.challenge.event.listener;

import com.trybe.moduleapi.challenge.event.model.ChallengeActionEvent;
import com.trybe.moduleapi.challenge.event.model.ChallengeParticipationEvent;
import com.trybe.moduleapi.challenge.event.type.ChallengeActionEventType;
import com.trybe.moduleapi.challenge.event.type.ChallengeParticipationEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
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
    public void handleChallengeActionEvent(ChallengeActionEvent event) {
        Challenge challenge = event.getChallenge();
        ChallengeActionEventType type = event.getEventType();
        Long userId = event.getUserId();

        int score = type.getScoreType().getPreferenceScore();

        if (type.isScoreUp()) {
            challengePreferenceCache.addPreference(userId, challenge, score);
        } else {
            challengePreferenceCache.removePreference(userId, challenge, score);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeParticipationEvent(ChallengeParticipationEvent event) {
        ChallengeParticipation participation = event.getParticipation();

        Challenge challenge = event.getChallenge();
        ChallengeParticipationEventType type = event.getEventType();
        Long userId = participation.getUser().getId();

        if (type.getScoreType() == null || type.getIsScoreUp() == null) return;

        int score = type.getScoreType().getPreferenceScore();

        if (type.getIsScoreUp()) {
            challengePreferenceCache.addPreference(userId, challenge, score);
        } else {
            challengePreferenceCache.removePreference(userId, challenge, score);
        }
    }
}