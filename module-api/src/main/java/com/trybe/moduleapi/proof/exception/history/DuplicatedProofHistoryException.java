package com.trybe.moduleapi.proof.exception.history;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicatedProofHistoryException extends BusinessException {
    public DuplicatedProofHistoryException() {
        super("해당 인증에 대한 기록이 이미 존재합니다.", HttpStatus.CONFLICT.value());
    }
}
