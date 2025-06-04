package com.trybe.moduleapi.challenge.event.model;

import com.trybe.moduleapi.challenge.event.type.ChallengeActionEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import lombok.Getter;

@Getter
public class ChallengeActionEvent extends ChallengeEvent {
    private final ChallengeActionEventType eventType;
    private final Long userId;

    public ChallengeActionEvent(Challenge challenge, ChallengeActionEventType eventType, Long userId) {
        super(challenge);
        this.eventType = eventType;
        this.userId = userId;
    }
}
