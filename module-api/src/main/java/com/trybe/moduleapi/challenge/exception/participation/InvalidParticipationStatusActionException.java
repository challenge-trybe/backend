package com.trybe.moduleapi.challenge.exception.participation;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidParticipationStatusActionException extends BusinessException {
    public InvalidParticipationStatusActionException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }
}
