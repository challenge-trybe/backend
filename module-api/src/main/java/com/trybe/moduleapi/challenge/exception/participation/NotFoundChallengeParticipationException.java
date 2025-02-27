package com.trybe.moduleapi.challenge.exception.participation;

import com.trybe.moduleapi.common.api.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class NotFoundChallengeParticipationException extends BusinessException {
    public NotFoundChallengeParticipationException() {
        super("존재하지 않는 챌린지 참여입니다.", HttpStatus.NOT_FOUND.value());
    }
}
