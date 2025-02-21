package com.trybe.moduleapi.challenge.dto;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;

import java.time.LocalDate;

public class ChallengeResponse {
    public record Detail(
            Long id,
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            ChallengeStatus status,
            int capacity,
            ChallengeCategory category,
            String proofWay,
            int proofCount
            // TODO: 현재 참여 인원수, 참여 인원 필드 추가
    ) {
        public static Detail from(Challenge challenge) {
            return new Detail(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getStartDate(),
                    challenge.getEndDate(),
                    challenge.getStatus(),
                    challenge.getCapacity(),
                    challenge.getCategory(),
                    challenge.getProof_way(),
                    challenge.getProof_count()
            );
        }
    }

    public record Summary(
            Long id,
            String title,
            String description,
            ChallengeStatus status,
            int capacity,
            ChallengeCategory category
            // TODO: 현재 참여 인원수 필드 추가
    ) {
        public static Summary from(Challenge challenge) {
            return new Summary(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getStatus(),
                    challenge.getCapacity(),
                    challenge.getCategory()
            );
        }
    }
}
