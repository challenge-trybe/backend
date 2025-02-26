package com.trybe.moduleapi.challenge.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidChallengeRoleActionException extends BusinessException {
    public InvalidChallengeRoleActionException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }
}
