package com.trybe.moduleapi.proof.exception.history;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidProofHistoryStatusException extends BusinessException {
    public InvalidProofHistoryStatusException(String message) {
        super(message, HttpStatus.CONFLICT.value());
    }
}
