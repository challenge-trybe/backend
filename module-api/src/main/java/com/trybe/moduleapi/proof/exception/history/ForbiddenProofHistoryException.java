package com.trybe.moduleapi.proof.exception.history;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenProofHistoryException extends BusinessException {
    public ForbiddenProofHistoryException(String message) {
        super(message, HttpStatus.FORBIDDEN.value());
    }
}
