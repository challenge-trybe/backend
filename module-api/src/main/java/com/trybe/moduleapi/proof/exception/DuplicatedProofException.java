package com.trybe.moduleapi.proof.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class DuplicatedProofException extends BusinessException {
    public DuplicatedProofException() {
        super("해당 날짜에 대한 인증이 이미 존재합니다.", HttpStatus.CONFLICT.value());
    }
}
