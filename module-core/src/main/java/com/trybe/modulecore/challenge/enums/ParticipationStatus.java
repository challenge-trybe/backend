package com.trybe.modulecore.challenge.enums;

import lombok.Getter;

@Getter
public enum ParticipationStatus {
    PENDING("대기"),
    ACCEPTED("수락"),
    REJECTED("거절"),
    DISABLED("탈퇴");

    private final String description;

    ParticipationStatus(String description) {
        this.description = description;
    }

    public boolean is(ParticipationStatus status) {
        return this == status;
    }

    public boolean isNot(ParticipationStatus status) {
        return this != status;
    }
}
