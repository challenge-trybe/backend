package com.trybe.moduleapi.user.exception;

public class UpdatePasswordFailException extends RuntimeException{
    public UpdatePasswordFailException(String message) {
        super(message);
    }
}
