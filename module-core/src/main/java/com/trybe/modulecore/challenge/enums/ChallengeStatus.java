package com.trybe.modulecore.challenge.enums;

import lombok.Getter;

@Getter
public enum ChallengeStatus {
    PENDING("대기중"),
    ONGOING("진행중"),
    DONE("종료");

    private final String description;

    ChallengeStatus(String description) {
        this.description = description;
    }

    public boolean is(ChallengeStatus status) {
        return this == status;
    }

    public boolean isNot(ChallengeStatus status) {
        return this != status;
    }
}
