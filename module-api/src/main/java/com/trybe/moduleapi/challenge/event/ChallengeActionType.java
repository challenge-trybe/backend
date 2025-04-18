package com.trybe.moduleapi.challenge.event;

import lombok.Getter;

@Getter
public enum ChallengeActionType {
    VIEW(1),
    BOOKMARK(10),
    PARTICIPATION(15);

    private final int score;

    ChallengeActionType(int score) {
        this.score = score;
    }
}
