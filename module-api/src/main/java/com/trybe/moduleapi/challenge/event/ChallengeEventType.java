package com.trybe.moduleapi.challenge.event;

import lombok.Getter;

@Getter
public enum ChallengeEventType {
    VIEW(ChallengeActionType.VIEW, true),
    BOOKMARK_ADD(ChallengeActionType.BOOKMARK, true),
    BOOKMARK_REMOVE(ChallengeActionType.BOOKMARK, false),
    PARTICIPATION_ADD(ChallengeActionType.PARTICIPATION, true),
    PARTICIPATION_REMOVE(ChallengeActionType.PARTICIPATION, false);

    private final ChallengeActionType actionType;
    private final boolean isScoreUp;

    ChallengeEventType(ChallengeActionType actionType, boolean isScoreUp) {
        this.actionType = actionType;
        this.isScoreUp = isScoreUp;
    }
}
