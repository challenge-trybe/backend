package com.trybe.moduleapi.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.auth.CustomUserDetailsService;
import com.trybe.moduleapi.auth.jwt.JwtUtils;
import com.trybe.moduleapi.auth.jwt.exception.CustomAccessDeniedHandler;
import com.trybe.moduleapi.auth.jwt.exception.CustomAuthenticationEntryPoint;
import com.trybe.moduleapi.config.SecurityConfig;
import com.trybe.moduleapi.token.request.RefreshTokenRequest;
import com.trybe.moduleapi.token.response.TokenResponse;
import com.trybe.moduleapi.user.dto.request.LoginRequest;
import com.trybe.moduleapi.user.exception.LoginFailedException;
import com.trybe.moduleapi.user.fixtures.AuthenticationFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.moduleapi.user.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
class AuthenticationControllerTest {
    private String docsPath = "auth-controller-test/";
    private final String invalidBadRequestPath = "invalid/bad-request/";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("로그인을 성공하면 200을 반환한다")
    void 로그인을_성공하면_200을_반환한다() throws Exception {
        LoginRequest 로그인_요청 = AuthenticationFixtures.로그인_요청;
        TokenResponse 토큰_반환 = AuthenticationFixtures.토큰_반환;
        when(authenticationService.login(any(LoginRequest.class))).thenReturn(토큰_반환);

        mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(로그인_요청)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.userResponse.id").value(UserFixtures.회원_응답.id()),
                       jsonPath("$.userResponse.nickname").value(UserFixtures.회원_응답.nickname()),
                       jsonPath("$.userResponse.email").value(UserFixtures.회원_응답.email()),
                       jsonPath("$.userResponse.userId").value(UserFixtures.회원_응답.userId()),
                       jsonPath("$.userResponse.gender").value(UserFixtures.회원_응답.gender().toString()),
                       jsonPath("$.userResponse.birth").value(UserFixtures.회원_응답.birth().toString()),
                       jsonPath("$.accessToken").value(토큰_반환.getAccessToken()),
                       jsonPath("$.refreshToken").value(토큰_반환.getRefreshToken()))
               .andDo(document(docsPath + "login",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("userId").description("User's email"),
                                       fieldWithPath("password").description("User's password")
                               ),
                               responseFields(
                                       fieldWithPath("userResponse.id").type(JsonFieldType.NUMBER).description("PK"),
                                       fieldWithPath("userResponse.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("userResponse.email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("userResponse.userId").type(JsonFieldType.STRING).description("아이디"),
                                       fieldWithPath("userResponse.gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("userResponse.birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)"),
                                       fieldWithPath("accessToken").description("Access token"),
                                       fieldWithPath("refreshToken").description("Refresh token")
                               )
               ));
    }

    @Test
    @DisplayName("로그인을 실패하면 401을 반환한다")
    void 로그인을_실패하면_401을_반환한다() throws Exception {
        LoginRequest 로그인_요청 = AuthenticationFixtures.로그인_요청;
        doThrow(new LoginFailedException()).when(authenticationService).login(로그인_요청);

        mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(로그인_요청)))
               .andExpect(status().isUnauthorized())
               .andExpectAll(
                       jsonPath("$.status").value(401),
                       jsonPath("$.message").exists(),
                       jsonPath("$.data").doesNotExist())
               .andDo(document(docsPath + "login/" + invalidBadRequestPath,
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("userId").description("유저 아이디"),
                                       fieldWithPath("password").description("비밀번호")
                               ),
                               responseFields(
                                       fieldWithPath("status").type(JsonFieldType.NUMBER).description("PK"),
                                       fieldWithPath("message").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("data").type(JsonFieldType.STRING).description("이메일").optional()
                               )
               ));
    }

    @Test
    @DisplayName("로그아웃을 성공하면 200을 반환한다.")
    public void 로그아웃을_성공하면_200을_반환한다() throws Exception {
        CustomUserDetails principalDetails = new CustomUserDetails(UserFixtures.회원);

        doNothing().when(authenticationService).logout(any(CustomUserDetails.class));

        mockMvc.perform(post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer "+AuthenticationFixtures.accessToken)
                                .with(SecurityMockMvcRequestPostProcessors.user(principalDetails)))
               .andExpect(status().isOk())
               .andDo(document(docsPath + "logout"));
    }

    @Test
    @DisplayName("토큰 재발급을 성공하면 200을 반환한다")
    void 토큰_재발급을_성공하면_200을_반환한다() throws Exception {
        RefreshTokenRequest 토큰_재발급_요청 = AuthenticationFixtures.토큰_재발급_요청;
        TokenResponse 토큰_반환 = AuthenticationFixtures.토큰_반환;

        when(authenticationService.tokenReissue(any(RefreshTokenRequest.class))).thenReturn(토큰_반환);

        mockMvc.perform(post("/api/v1/auth/token/reissue")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(토큰_재발급_요청)))
               .andExpect(status().isOk())
               .andExpectAll(
                       jsonPath("$.userResponse.id").value(UserFixtures.회원_응답.id()),
                       jsonPath("$.userResponse.nickname").value(UserFixtures.회원_응답.nickname()),
                       jsonPath("$.userResponse.email").value(UserFixtures.회원_응답.email()),
                       jsonPath("$.userResponse.userId").value(UserFixtures.회원_응답.userId()),
                       jsonPath("$.userResponse.gender").value(UserFixtures.회원_응답.gender().toString()),
                       jsonPath("$.userResponse.birth").value(UserFixtures.회원_응답.birth().toString()),
                       jsonPath("$.accessToken").value(토큰_반환.getAccessToken()),
                       jsonPath("$.refreshToken").value(토큰_반환.getRefreshToken()))
               .andDo(document(docsPath + "token-reissue",
                               preprocessRequest(prettyPrint()),
                               preprocessResponse(prettyPrint()),
                               requestFields(
                                       fieldWithPath("refreshToken").description("리프레쉬 토큰")
                               ),
                               responseFields(
                                       fieldWithPath("userResponse.id").type(JsonFieldType.NUMBER).description("PK"),
                                       fieldWithPath("userResponse.nickname").type(JsonFieldType.STRING).description("닉네임"),
                                       fieldWithPath("userResponse.email").type(JsonFieldType.STRING).description("이메일"),
                                       fieldWithPath("userResponse.userId").type(JsonFieldType.STRING).description("아이디"),
                                       fieldWithPath("userResponse.gender").type(JsonFieldType.STRING).description("성별"),
                                       fieldWithPath("userResponse.birth").type(JsonFieldType.STRING).description("생년월일 (형식: YYYY-MM-DD)"),
                                       fieldWithPath("accessToken").description("Access token"),
                                       fieldWithPath("refreshToken").description("Refresh token")
                               )
               ));
    }
}
