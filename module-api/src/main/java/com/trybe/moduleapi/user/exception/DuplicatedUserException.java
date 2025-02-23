package com.trybe.moduleapi.user.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicatedUserException extends BusinessException {
    public DuplicatedUserException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}
