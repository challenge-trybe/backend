package com.trybe.moduleapi.proof.exception.history;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundProofHistoryException extends BusinessException {
    public NotFoundProofHistoryException() {
        super("존재하지 않는 인증 기록입니다.", HttpStatus.NOT_FOUND.value());
    }
}
