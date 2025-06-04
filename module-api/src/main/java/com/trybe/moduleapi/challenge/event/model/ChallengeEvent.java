package com.trybe.moduleapi.challenge.event.model;

import com.trybe.modulecore.challenge.entity.Challenge;
import lombok.Getter;

@Getter
public class ChallengeEvent {
    private final Challenge challenge;

    public ChallengeEvent(Challenge challenge) {
        this.challenge = challenge;
    }
}
