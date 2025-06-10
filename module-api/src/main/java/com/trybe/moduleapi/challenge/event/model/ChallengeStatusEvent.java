package com.trybe.moduleapi.challenge.event.model;

import com.trybe.moduleapi.challenge.event.type.ChallengeStatusEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ChallengeStatusEvent extends ChallengeEvent {
    private final ChallengeStatusEventType eventType;
    private final List<User> participants;

    public ChallengeStatusEvent(Challenge challenge, ChallengeStatusEventType eventType, List<User> participants) {
        super(challenge);
        this.eventType = eventType;
        this.participants = participants;
    }
}
