package com.trybe.moduleapi.challenge.event.type;

import lombok.Getter;

@Getter
public enum ChallengeParticipationEventType {
    PARTICIPATION_ADD(ChallengeScoreType.PARTICIPATION, true),
    PARTICIPATION_REMOVE(ChallengeScoreType.PARTICIPATION, false),
    PARTICIPATION_PROCESSED(null, null)
    ;

    private final ChallengeScoreType scoreType;
    private final Boolean isScoreUp;

    ChallengeParticipationEventType(ChallengeScoreType scoreType, Boolean isScoreUp) {
        this.scoreType = scoreType;
        this.isScoreUp = isScoreUp;
    }
}
