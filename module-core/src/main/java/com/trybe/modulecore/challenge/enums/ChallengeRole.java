package com.trybe.modulecore.challenge.enums;

import lombok.Getter;

@Getter
public enum ChallengeRole {
    MEMBER("참가자"), LEADER("리더");
    private String description;

    ChallengeRole(String description) {
        this.description = description;
    }
}
