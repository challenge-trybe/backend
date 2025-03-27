package com.trybe.moduleapi.notification.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundNotificationException extends BusinessException {
    public NotFoundNotificationException() {
        super("존재하지 않는 알림입니다.", HttpStatus.NOT_FOUND.value());
    }
}
