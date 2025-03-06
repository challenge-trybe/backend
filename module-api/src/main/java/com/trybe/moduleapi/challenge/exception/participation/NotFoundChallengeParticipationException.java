package com.trybe.moduleapi.challenge.exception.participation;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundChallengeParticipationException extends BusinessException {
    public NotFoundChallengeParticipationException(String message) {
        super(message, HttpStatus.NOT_FOUND.value());
    }
}
