package com.trybe.moduleapi.proof.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class InvalidProofDateException extends BusinessException {
    public InvalidProofDateException() {
        super("인증 기록은 인증 날짜에만 등록할 수 있습니다.", HttpStatus.BAD_REQUEST.value());
    }
}
