package com.trybe.moduleapi.user.fixtures;

import com.trybe.moduleapi.token.request.RefreshTokenRequest;
import com.trybe.moduleapi.token.response.TokenResponse;
import com.trybe.moduleapi.user.dto.request.LoginRequest;
import com.trybe.moduleapi.user.dto.request.UserRequest;

public class AuthenticationFixtures {
    public static final String 로그인_아이디 = UserFixtures.회원_아이디;
    public static final String 비밀번호 = UserFixtures.회원_비밀번호;
    public static final String 잘못된_로그인_아이디 = "";
    public static final String 잘못된_비밀번호 = UserFixtures.잘못된_회원_비밀번호;

    public static final String accessToken = "accessToken";
    public static final String refreshToken = "refreshToken";

    public static LoginRequest 로그인_요청 = new LoginRequest(로그인_아이디,비밀번호);
    public static LoginRequest 잘못된_로그인_요청 = new LoginRequest(잘못된_로그인_아이디,잘못된_비밀번호);
    public static TokenResponse 토큰_반환 = TokenResponse.from(UserFixtures.요약_회원_응답, accessToken, refreshToken);
    public static RefreshTokenRequest 토큰_재발급_요청 = new RefreshTokenRequest(refreshToken);
}
