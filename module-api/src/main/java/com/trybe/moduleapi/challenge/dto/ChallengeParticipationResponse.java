package com.trybe.moduleapi.challenge.dto;

import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;

import java.time.LocalDateTime;

public class ChallengeParticipationResponse {
    public record Summary(
            Long id,
            UserResponse user,
            ChallengeRole role,
            ParticipationStatus status,
            LocalDateTime createdAt
    ) {
        public static Summary from(ChallengeParticipation challengeParticipation) {
            return new Summary(
                    challengeParticipation.getId(),
                    UserResponse.from(challengeParticipation.getUser()),
                    challengeParticipation.getRole(),
                    challengeParticipation.getStatus(),
                    challengeParticipation.getCreatedAt()
            );
        }
    }

    public record Detail(
            ChallengeResponse.Summary challenge,
            Long id,
            UserResponse user,
            ChallengeRole role,
            ParticipationStatus status,
            LocalDateTime createdAt
    ) {
        public static Detail from(ChallengeParticipation challengeParticipation) {
            return new Detail(
                    ChallengeResponse.Summary.from(challengeParticipation.getChallenge()),
                    challengeParticipation.getId(),
                    UserResponse.from(challengeParticipation.getUser()),
                    challengeParticipation.getRole(),
                    challengeParticipation.getStatus(),
                    challengeParticipation.getCreatedAt()
            );
        }
    }
}
