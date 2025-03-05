package com.trybe.moduleapi.proof.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidProofDeletionException extends BusinessException {
    public InvalidProofDeletionException(String message) {
        super(message, HttpStatus.BAD_REQUEST.value());
    }
}
