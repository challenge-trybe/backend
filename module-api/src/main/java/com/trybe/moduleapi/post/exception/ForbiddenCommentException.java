package com.trybe.moduleapi.post.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenCommentException extends BusinessException {
    public ForbiddenCommentException() {
        super("자신이 작성한 댓글만 삭제할 수 있습니다.", HttpStatus.FORBIDDEN.value());
    }
}
