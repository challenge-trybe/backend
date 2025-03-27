package com.trybe.moduleapi.notification.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ForbiddenNotificationException extends BusinessException {
    public ForbiddenNotificationException() {
        super("해당 알림에 대한 권한이 없습니다.", HttpStatus.FORBIDDEN.value());
    }
}
