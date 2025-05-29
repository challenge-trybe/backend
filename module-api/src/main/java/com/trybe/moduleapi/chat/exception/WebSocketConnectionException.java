package com.trybe.moduleapi.chat.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class WebSocketConnectionException extends BusinessException {
    public WebSocketConnectionException() {
        super("웹소켓 연결 시 헤더 오류", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
