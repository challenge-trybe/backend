package com.trybe.moduleapi.proof.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundProofException extends BusinessException {
    public NotFoundProofException() {
        super("존재하지 않는 인증입니다.", HttpStatus.NOT_FOUND.value());
    }
}
