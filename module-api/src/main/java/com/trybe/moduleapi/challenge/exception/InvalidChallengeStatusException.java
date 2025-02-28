package com.trybe.moduleapi.challenge.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidChallengeStatusException extends BusinessException {
    public InvalidChallengeStatusException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }
}
