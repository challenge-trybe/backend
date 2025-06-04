package com.trybe.moduleapi.challenge.event.type;

import lombok.Getter;

@Getter
public enum ChallengeActionEventType {
    CREATE(ChallengeScoreType.CREATE, true),
    VIEW(ChallengeScoreType.VIEW, true),
    BOOKMARK_ADD(ChallengeScoreType.BOOKMARK, true),
    BOOKMARK_REMOVE(ChallengeScoreType.BOOKMARK, false),
    ;

    private final ChallengeScoreType scoreType;
    private final boolean isScoreUp;

    ChallengeActionEventType(ChallengeScoreType scoreType, boolean isScoreUp) {
        this.scoreType = scoreType;
        this.isScoreUp = isScoreUp;
    }
}
