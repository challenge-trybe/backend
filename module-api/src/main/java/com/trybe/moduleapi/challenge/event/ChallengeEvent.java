package com.trybe.moduleapi.challenge.event;

import lombok.Getter;

@Getter
public class ChallengeEvent {
    private final Long challengeId;
    private final Long userId;
    private final ChallengeEventType type;

    public ChallengeEvent(Long challengeId, Long userId, ChallengeEventType type) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.type = type;
    }
}
