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
            ChallengeCategory category,
            int capacity,
            int participantCount,
            String proofWay,
            int proofCount,
            Boolean bookmarked
    ) {
        public static Detail from(Challenge challenge, int participantCount, Boolean bookmarked) {
            return new Detail(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getStartDate(),
                    challenge.getEndDate(),
                    challenge.getStatus(),
                    challenge.getCategory(),
                    challenge.getCapacity(),
                    participantCount,
                    challenge.getProofWay(),
                    challenge.getProofCount(),
                    bookmarked
            );
        }
    }

    public record Summary(
            Long id,
            String title,
            String description,
            ChallengeStatus status,
            ChallengeCategory category,
            int capacity,
            int participantCount,
            Boolean bookmarked
    ) {
        public static Summary from(Challenge challenge, int participantCount, Boolean bookmarked) {
            return new Summary(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getStatus(),
                    challenge.getCategory(),
                    challenge.getCapacity(),
                    participantCount,
                    bookmarked
            );
        }
    }

    public record Bookmark(
            int bookmarkCount,
            boolean bookmarked
    ) { }
}
