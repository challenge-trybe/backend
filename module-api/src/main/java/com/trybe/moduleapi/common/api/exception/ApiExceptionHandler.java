package com.trybe.moduleapi.common.api.exception;

import com.trybe.moduleapi.common.api.ApiErrorResponse;
import com.trybe.moduleapi.user.exception.DuplicatedUserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex) {
        ApiErrorResponse response = new ApiErrorResponse(ex.getStatus(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(ex.getStatus())).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        ApiErrorResponse response = new ApiErrorResponse(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
