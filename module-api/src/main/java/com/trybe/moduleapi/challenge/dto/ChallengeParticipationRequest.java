package com.trybe.moduleapi.challenge.dto;

import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import jakarta.validation.constraints.NotNull;

public class ChallengeParticipationRequest {
    public static final String STATUS_NOT_NULL = "처리할 상태를 입력해주세요.";

    public record Confirm(
            @NotNull(message = STATUS_NOT_NULL)
            ParticipationStatus status
    ) { }
}
