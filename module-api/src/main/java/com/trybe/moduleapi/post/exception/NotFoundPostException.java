package com.trybe.moduleapi.post.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

public class NotFoundPostException extends BusinessException {
    public NotFoundPostException() {
        super("존재하지 않는 포스트입니다.", HttpStatus.NOT_FOUND.value());
    }
}
