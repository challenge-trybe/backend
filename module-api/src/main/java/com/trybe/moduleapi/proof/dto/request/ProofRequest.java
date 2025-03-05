package com.trybe.moduleapi.proof.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ProofRequest {
    private static final String CHALLENGE_ID_NOT_NULL_MESSAGE = "챌린지를 선택해주세요.";

    private static final String DATE_NOT_NULL_MESSAGE = "인증 날짜를 입력해주세요.";
    private static final String DATE_AFTER_TODAY_MESSAGE = "인증 날짜는 현재 날짜 이후여야 합니다.";

    public record Create(
            @NotNull(message = CHALLENGE_ID_NOT_NULL_MESSAGE)
            Long challengeId,

            @NotNull(message = DATE_NOT_NULL_MESSAGE)
            @Future(message = DATE_AFTER_TODAY_MESSAGE)
            LocalDate date
    ) {
    }
}
