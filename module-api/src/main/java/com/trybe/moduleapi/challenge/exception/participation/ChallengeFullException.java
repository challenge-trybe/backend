package com.trybe.moduleapi.challenge.exception.participation;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ChallengeFullException extends BusinessException {
    public ChallengeFullException() {
        super("챌린지 인원이 가득 찼습니다.", HttpStatus.CONFLICT.value());
    }
}
