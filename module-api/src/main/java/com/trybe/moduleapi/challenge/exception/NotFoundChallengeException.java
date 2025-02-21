package com.trybe.moduleapi.challenge.exception;

public class NotFoundChallengeException extends RuntimeException{
    public NotFoundChallengeException() {
        super("존재하지 않는 챌린지입니다.");
    }
}
