package com.trybe.moduleapi.user.exception;

public class DuplicatedUserException extends RuntimeException{
    public DuplicatedUserException(String message) {
        super(message);
    }
}
