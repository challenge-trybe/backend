package com.trybe.moduleapi.common.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@Order(0)
public class RequestLoggingFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(cachingRequest, cachingResponse);

        String clientIp = request.getRemoteAddr();
        String method = request.getMethod();
        String url = request.getRequestURI();
        String requestBody = cachingRequest.getContentAsString();
        String responseBody = new String(cachingResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

        log.info("[요청] clientIp={} method={} url={} body={}", clientIp, method, url, requestBody);

        log.info("[응답] clientIp={} method={} url={} body={}", clientIp, method, url, responseBody);

        cachingResponse.copyBodyToResponse();
    }
}
