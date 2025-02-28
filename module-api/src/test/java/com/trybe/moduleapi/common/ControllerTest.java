package com.trybe.moduleapi.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trybe.moduleapi.auth.CustomUserDetailsService;
import com.trybe.moduleapi.auth.jwt.JwtUtils;
import com.trybe.moduleapi.auth.jwt.exception.CustomAccessDeniedHandler;
import com.trybe.moduleapi.auth.jwt.exception.CustomAuthenticationEntryPoint;
import com.trybe.moduleapi.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Import(SecurityConfig.class)
public abstract class ControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    protected JwtUtils jwtUtils;

    @MockitoBean
    protected CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    protected CustomAccessDeniedHandler customAccessDeniedHandler;
}
