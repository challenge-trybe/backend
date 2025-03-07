package com.trybe.moduleapi.proof.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ProofCountExceededException extends BusinessException {
    public ProofCountExceededException(int count) {
        super("인증 횟수 " + count + "회를 초과하여 생성할 수 없습니다." , HttpStatus.BAD_REQUEST.value());
    }
}
