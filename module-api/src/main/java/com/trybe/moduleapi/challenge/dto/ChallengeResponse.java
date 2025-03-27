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
            Bookmark bookmark
    ) {
        public static Detail from(Challenge challenge, int participantCount, Bookmark bookmark) {
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
                    bookmark
            );
        }
    }

    public record Preview(
            Long id,
            String title,
            String description,
            ChallengeStatus status,
            ChallengeCategory category,
            int capacity,
            int participantCount,
            Bookmark bookmark
    ) {
        public static Preview from(Challenge challenge, int participantCount, Bookmark bookmark) {
            return new Preview(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getStatus(),
                    challenge.getCategory(),
                    challenge.getCapacity(),
                    participantCount,
                    bookmark
            );
        }
    }

    public record Summary(
            Long id,
            String title,
            String description,
            ChallengeStatus status,
            ChallengeCategory category,
            int capacity
    ) {
        public static Summary from(Challenge challenge) {
            return new Summary(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getStatus(),
                    challenge.getCategory(),
                    challenge.getCapacity()
            );
        }
    }

    public record Bookmark(
            int bookmarkCount,
            Boolean bookmarked
    ) { }
}
