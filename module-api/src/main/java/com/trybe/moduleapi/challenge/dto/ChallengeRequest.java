package com.trybe.moduleapi.challenge.dto;

import com.trybe.moduleapi.annotation.NotEmptyList;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ChallengeRequest {
    private static final String TITLE_NOT_BLANK_MESSAGE = "챌린지 제목을 입력해주세요.";
    private static final int TITLE_MAX_LENGTH = 100;
    private static final String TITLE_MAX_LENGTH_MESSAGE = "챌린지 제목은 최대 100자까지 입력 가능합니다.";

    private static final String DESCRIPTION_NOT_BLANK_MESSAGE = "챌린지 설명을 입력해주세요.";
    private static final int DESCRIPTION_MAX_LENGTH = 1000;
    private static final String DESCRIPTION_MAX_LENGTH_MESSAGE = "챌린지 설명은 최대 1,000자까지 입력 가능합니다.";

    private static final String START_DATE_NOT_NULL_MESSAGE = "시작일을 입력해주세요.";
    private static final String END_DATE_NOT_NULL_MESSAGE = "종료일을 입력해주세요.";
    private static final String START_DATE_BEFORE_END_DATE_MESSAGE = "시작일은 종료일보다 빠를 수 없습니다.";
    private static final String DATES_AFTER_TODAY_MESSAGE = "시작일과 종료일은 모두 현재 날짜 이후여야 합니다.";
    private static final String DURATION_LIMIT_MESSAGE = "챌린지는 최소 1주, 최대 8주까지 진행 가능합니다.";

    private static final String CAPACITY_NOT_NULL_MESSAGE = "챌린지 인원을 입력해주세요.";
    private static final int CAPACITY_MIN = 1;
    private static final String CAPACITY_MIN_MESSAGE = "챌린지 인원은 최소 1명 이상이어야 합니다.";
    private static final int CAPACITY_MAX = 10;
    private static final String CAPACITY_MAX_MESSAGE = "챌린지 인원은 최대 10명까지 가능합니다.";

    private static final String CATEGORY_NOT_NULL_MESSAGE = "챌린지 카테고리를 선택해주세요.";

    private static final String PROOF_WAY_NOT_BLANK_MESSAGE = "인증 방법을 입력해주세요.";
    private static final int PROOF_WAY_MAX_LENGTH = 500;
    private static final String PROOF_WAY_MAX_LENGTH_MESSAGE = "인증 방법은 최대 500자까지 입력 가능합니다.";

    private static final String PROOF_COUNT_NOT_NULL_MESSAGE = "인증 횟수를 입력해주세요.";
    private static final int PROOF_COUNT_MIN = 1;
    private static final String PROOF_COUNT_MIN_MESSAGE = "인증 횟수는 최소 1회 이상이어야 합니다.";
    private static final int PROOF_COUNT_MAX = 30;
    private static final String PROOF_COUNT_MAX_MESSAGE = "인증 횟수는 최대 30회까지 가능합니다.";

    private static final String CATEGORIES_NOT_EMPTY_MESSAGE = "챌린지 카테고리를 선택해주세요.";
    private static final String STATUSES_NOT_EMPTY_MESSAGE = "챌린지 상태를 선택해주세요.";

    public record Create(
            @NotBlank(message = TITLE_NOT_BLANK_MESSAGE)
            @Size(max = TITLE_MAX_LENGTH, message = TITLE_MAX_LENGTH_MESSAGE)
            String title,

            @NotBlank(message = DESCRIPTION_NOT_BLANK_MESSAGE)
            @Size(max = DESCRIPTION_MAX_LENGTH, message = DESCRIPTION_MAX_LENGTH_MESSAGE)
            String description,

            @NotNull(message = START_DATE_NOT_NULL_MESSAGE)
            LocalDate startDate,

            @NotNull(message = END_DATE_NOT_NULL_MESSAGE)
            LocalDate endDate,

            @NotNull(message = CAPACITY_NOT_NULL_MESSAGE)
            @Min(value = CAPACITY_MIN, message = CAPACITY_MIN_MESSAGE)
            @Max(value = CAPACITY_MAX, message = CAPACITY_MAX_MESSAGE)
            int capacity,

            @NotNull(message = CATEGORY_NOT_NULL_MESSAGE)
            ChallengeCategory category,

            @NotBlank(message = PROOF_WAY_NOT_BLANK_MESSAGE)
            @Size(max = PROOF_WAY_MAX_LENGTH, message = PROOF_WAY_MAX_LENGTH_MESSAGE)
            String proofWay,

            @NotNull(message = PROOF_COUNT_NOT_NULL_MESSAGE)
            @Min(value = PROOF_COUNT_MIN, message = PROOF_COUNT_MIN_MESSAGE)
            @Max(value = PROOF_COUNT_MAX, message = PROOF_COUNT_MAX_MESSAGE)
            int proofCount
    ) {
        @AssertTrue(message = START_DATE_BEFORE_END_DATE_MESSAGE)
        private boolean isStartDateBeforeEndDate() {
            return startDate.isBefore(endDate);
        }

        @AssertTrue(message = DATES_AFTER_TODAY_MESSAGE)
        private boolean isDatesAfterToday() {
            return startDate.isAfter(LocalDate.now()) && endDate.isAfter(LocalDate.now());
        }

        @AssertTrue(message = DURATION_LIMIT_MESSAGE)
        private boolean isDurationLimit() {
            long durationInDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            return durationInDays >= 7 && durationInDays <= 56;
        }

        public Challenge toEntity() {
            return new Challenge(
                    title,
                    description,
                    startDate,
                    endDate,
                    capacity,
                    category,
                    proofWay,
                    proofCount
            );
        }
    }

    public record Read(
            // TODO: Filter
            @NotEmptyList(message = STATUSES_NOT_EMPTY_MESSAGE)
            List<ChallengeStatus> statuses,

            @NotEmptyList(message = CATEGORIES_NOT_EMPTY_MESSAGE)
            List<ChallengeCategory> categories
    ) { }

    public record UpdateContent(
            @NotBlank(message = TITLE_NOT_BLANK_MESSAGE)
            @Size(max = TITLE_MAX_LENGTH, message = TITLE_MAX_LENGTH_MESSAGE)
            String title,

            @NotBlank(message = DESCRIPTION_NOT_BLANK_MESSAGE)
            @Size(max = DESCRIPTION_MAX_LENGTH, message = DESCRIPTION_MAX_LENGTH_MESSAGE)
            String description,

            @NotNull(message = START_DATE_NOT_NULL_MESSAGE)
            LocalDate startDate,

            @NotNull(message = END_DATE_NOT_NULL_MESSAGE)
            LocalDate endDate,

            @NotNull(message = CAPACITY_NOT_NULL_MESSAGE)
            @Min(value = CAPACITY_MIN, message = CAPACITY_MIN_MESSAGE)
            @Max(value = CAPACITY_MAX, message = CAPACITY_MAX_MESSAGE)
            int capacity,

            @NotNull(message = CATEGORY_NOT_NULL_MESSAGE)
            ChallengeCategory category
    ) {
        @AssertTrue(message = START_DATE_BEFORE_END_DATE_MESSAGE)
        private boolean isStartDateBeforeEndDate() {
            return startDate.isBefore(endDate);
        }

        @AssertTrue(message = DATES_AFTER_TODAY_MESSAGE)
        private boolean isDatesAfterToday() {
            return startDate.isAfter(LocalDate.now()) && endDate.isAfter(LocalDate.now());
        }

        @AssertTrue(message = DURATION_LIMIT_MESSAGE)
        private boolean isDurationLimit() {
            long durationInDays = ChronoUnit.DAYS.between(startDate, endDate);
            return durationInDays >= 7 && durationInDays <= 56;
        }
    }

    public record UpdateProof(
            @NotBlank(message = PROOF_WAY_NOT_BLANK_MESSAGE)
            @Size(max = PROOF_WAY_MAX_LENGTH, message = PROOF_WAY_MAX_LENGTH_MESSAGE)
            String proofWay,

            @NotNull(message = PROOF_COUNT_NOT_NULL_MESSAGE)
            @Min(value = PROOF_COUNT_MIN, message = PROOF_COUNT_MIN_MESSAGE)
            @Max(value = PROOF_COUNT_MAX, message = PROOF_COUNT_MAX_MESSAGE)
            int proofCount
    ) { }
}