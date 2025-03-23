package com.trybe.moduleapi.post.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenCommentException extends BusinessException {
    public ForbiddenCommentException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }
}
