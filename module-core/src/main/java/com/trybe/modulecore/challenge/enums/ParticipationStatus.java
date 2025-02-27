package com.trybe.modulecore.challenge.enums;

import lombok.Getter;

@Getter
public enum ParticipationStatus {
    PENDING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    DISABLED("탈퇴됨");

    private String description;

    ParticipationStatus(String description) {
        this.description = description;
    }
}
