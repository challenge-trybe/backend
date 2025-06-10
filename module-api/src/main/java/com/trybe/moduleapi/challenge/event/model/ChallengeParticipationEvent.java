package com.trybe.moduleapi.challenge.event.model;

import com.trybe.moduleapi.challenge.event.type.ChallengeParticipationEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.user.entity.User;
import lombok.Getter;

@Getter
public class ChallengeParticipationEvent extends ChallengeEvent {
    private final ChallengeParticipationEventType eventType;
    private final ChallengeParticipation participation;
    private final User leader;

    public ChallengeParticipationEvent(Challenge challenge, ChallengeParticipationEventType eventType, ChallengeParticipation participation, User leader) {
        super(challenge);
        this.eventType = eventType;
        this.participation = participation;
        this.leader = leader;
    }
}
