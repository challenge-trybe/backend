package com.trybe.moduleapi.challenge.event.type;

import lombok.Getter;

@Getter
public enum ChallengeEventType {
    CREATE(ChallengeScoreType.CREATE, true),
    VIEW(ChallengeScoreType.VIEW, true),
    BOOKMARK_ADD(ChallengeScoreType.BOOKMARK, true),
    BOOKMARK_REMOVE(ChallengeScoreType.BOOKMARK, false),
    PARTICIPATION_ADD(ChallengeScoreType.PARTICIPATION, true),
    PARTICIPATION_REMOVE(ChallengeScoreType.PARTICIPATION, false);

    private final ChallengeScoreType actionType;
    private final boolean isScoreUp;

    ChallengeEventType(ChallengeScoreType actionType, boolean isScoreUp) {
        this.actionType = actionType;
        this.isScoreUp = isScoreUp;
    }
}
