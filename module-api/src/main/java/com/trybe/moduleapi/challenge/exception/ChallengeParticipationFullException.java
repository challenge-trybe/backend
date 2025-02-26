package com.trybe.moduleapi.challenge.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ChallengeParticipationFullException extends BusinessException {
    public ChallengeParticipationFullException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}
