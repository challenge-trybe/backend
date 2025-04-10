package com.trybe.moduleapi.chat.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundChatRoomException extends BusinessException {
    public NotFoundChatRoomException() {
        super("해당 챌린지에 대한 채팅방은 존재하지 않습니다.", HttpStatus.NOT_FOUND.value());
    }
}
