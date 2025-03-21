package com.trybe.moduleapi.post.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundCommentException extends BusinessException {
    public NotFoundCommentException() {
        super("존재하지 않는 댓글입니다.", HttpStatus.NOT_FOUND.value());
    }
}
