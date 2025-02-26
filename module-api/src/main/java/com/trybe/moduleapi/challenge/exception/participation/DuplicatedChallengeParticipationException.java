package com.trybe.moduleapi.challenge.exception.participation;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicatedChallengeParticipationException extends BusinessException {
    public DuplicatedChallengeParticipationException() {
        super("챌린지 참여 정보가 존재합니다.", HttpStatus.CONFLICT.value());
    }
}
