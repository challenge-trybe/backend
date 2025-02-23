package com.trybe.moduleapi.user.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class UpdatePasswordFailException extends BusinessException {
    public UpdatePasswordFailException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }
}
