package com.trybe.moduleapi.post.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class AlreadyLikePostException extends BusinessException {
    public AlreadyLikePostException() {
        super("이미 좋아요 누른 게시글입니다.", HttpStatus.CONTINUE.value());
    }
}
