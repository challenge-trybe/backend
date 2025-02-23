package com.trybe.moduleapi.user.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundUserException extends BusinessException {
    public NotFoundUserException() {
        super("존재하지 않는 회원입니다.", HttpStatus.NOT_FOUND.value());
    }
}
