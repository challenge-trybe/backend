package com.trybe.moduleapi.user.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class LoginFailedException extends BusinessException {
    public LoginFailedException() {
        super("아이디 또는 비밀번호가 틀렸습니다.", HttpStatus.UNAUTHORIZED.value());
    }
}
