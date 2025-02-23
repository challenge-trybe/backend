package com.trybe.moduleapi.common.api;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ApiErrorResponse {
    private final int status;
    private final String message;
    private final Map<String, String> data;

    public ApiErrorResponse(int status, String message, Map<String, String> data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ApiErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.data = null;
    }

    public ApiErrorResponse(Map<String, String> data) {
        this.status = HttpStatus.BAD_REQUEST.value();
        this.message = null;
        this.data = data;
    }
}
