package com.trybe.modulecore.challenge.enums;

public enum ChallengeCategory {
    SPORTS("운동"),
    HOBBY("취미"),
    DIET("식단"),
    SAVING("저축"),
    STUDY("공부"),
    LIFE("생활");

    private final String description;

    ChallengeCategory(String description) {
        this.description = description;
    }
}
