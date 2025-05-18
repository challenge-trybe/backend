package com.trybe.moduleapi.chat.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundChatRoomException extends BusinessException {
    public NotFoundChatRoomException() {
        super("채팅방이 존재하지 않습니다.", HttpStatus.NOT_FOUND.value());
    }
}
