package com.trybe.moduleapi.auth.jwt.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trybe.moduleapi.common.api.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), "접근할 수 없는 서비스입니다.");
        String errorMessage = objectMapper.writeValueAsString(apiErrorResponse);
        response.getWriter().write(errorMessage);
    }
}
