package com.trybe.moduleapi.post.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenPostException extends BusinessException {
    public ForbiddenPostException() {
        super("해당 게시글에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN.value());
    }
}
