package com.trybe.moduleapi.challenge.exception;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundChallengeException extends BusinessException {
    public NotFoundChallengeException() {
        super("존재하지 않는 챌린지입니다.", HttpStatus.NOT_FOUND.value());
    }
}
