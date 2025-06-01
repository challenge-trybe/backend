package com.trybe.moduleapi.challenge.event.model;

import com.trybe.moduleapi.challenge.event.type.ChallengeEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import lombok.Getter;

@Getter
public class ChallengeEvent {
    private final Challenge challenge;
    private final Long userId;
    private final ChallengeEventType type;

    public ChallengeEvent(Challenge challenge, Long userId, ChallengeEventType type) {
        this.challenge = challenge;
        this.userId = userId;
        this.type = type;
    }
}
