package com.trybe.moduleapi.challenge.event.type;

import lombok.Getter;

@Getter
public enum ChallengeActionType {
    CREATE(0, 10),
    VIEW(1, 1),
    BOOKMARK(10, 10),
    PARTICIPATION(15, 15);

    private final int popularityScore;
    private final int preferenceScore;

    ChallengeActionType(int popularityScore, int preferenceScore) {
        this.popularityScore = popularityScore;
        this.preferenceScore = preferenceScore;
    }
}
