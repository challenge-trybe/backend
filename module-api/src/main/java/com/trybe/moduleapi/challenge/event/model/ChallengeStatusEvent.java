package com.trybe.moduleapi.challenge.event.model;

import com.trybe.moduleapi.challenge.event.type.ChallengeStatusEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import lombok.Getter;

@Getter
public class ChallengeStatusEvent extends ChallengeEvent {
    private final ChallengeStatusEventType eventType;

    public ChallengeStatusEvent(Challenge challenge, ChallengeStatusEventType eventType) {
        super(challenge);
        this.eventType = eventType;
    }
}
