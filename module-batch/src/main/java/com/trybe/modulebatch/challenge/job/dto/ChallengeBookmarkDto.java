package com.trybe.modulebatch.challenge.job.dto;

import lombok.Getter;

@Getter
public class ChallengeBookmarkDto {
    public ChallengeBookmarkDto(Long userId, Long challengeId) {
        this.challengeId = challengeId;
        this.userId = userId;
    }

    private final Long challengeId;
    private final Long userId;
}
