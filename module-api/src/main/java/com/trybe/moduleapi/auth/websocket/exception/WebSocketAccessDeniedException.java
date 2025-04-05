package com.trybe.moduleapi.auth.websocket.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class WebSocketAccessDeniedException extends BusinessException {
    public WebSocketAccessDeniedException() {
        super("로그인이 필요한 서비스입니다.", HttpStatus.FORBIDDEN.value());
    }
}
