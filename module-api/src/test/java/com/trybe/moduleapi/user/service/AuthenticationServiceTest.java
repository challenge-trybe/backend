package com.trybe.moduleapi.user.service;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.auth.jwt.JwtUtils;
import com.trybe.moduleapi.token.response.TokenResponse;
import com.trybe.moduleapi.user.dto.request.LoginRequest;
import com.trybe.moduleapi.user.exception.LoginFailedException;
import com.trybe.moduleapi.user.fixtures.AuthenticationFixtures;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetails customUserDetails;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("로그인을 성공하면 유저 정보와 토큰을 반환한다.")
    void 로그인_성공하면_유저정보와_토큰을_반환한다 () {
        /* given */
        LoginRequest request = AuthenticationFixtures.로그인_요청;
        UserFixtures.회원.updatePassword(UserFixtures.회원_암호화된_비밀번호);
        Mockito.when(userRepository.findByUserId(request.userId())).thenReturn(Optional.of(UserFixtures.회원));
        Mockito.when(passwordEncoder.matches(request.password(), UserFixtures.회원_암호화된_비밀번호)).thenReturn(true);
        Mockito.when(jwtUtils.generateToken(UserFixtures.회원_아이디, UserFixtures.권한))
               .thenReturn(Map.of("accessToken", AuthenticationFixtures.accessToken, "refreshToken", AuthenticationFixtures.refreshToken));
        /* when */
        TokenResponse tokenResponse = authenticationService.login(request);

        /* then */
        assertEquals(tokenResponse.getAccessToken(), AuthenticationFixtures.accessToken);
        assertEquals(tokenResponse.getRefreshToken(), AuthenticationFixtures.refreshToken);
    }

    @Test
    @DisplayName("로그인을 실패하면 에러를 반환한다.")
    void 로그인_실패하면_에러를_반환한다 () {
        /* given */
        LoginRequest request = AuthenticationFixtures.로그인_요청;
        UserFixtures.회원.updatePassword(UserFixtures.새로운_암호화된_비밀번호);
        Mockito.when(userRepository.findByUserId(UserFixtures.회원_아이디)).thenReturn(Optional.of(UserFixtures.회원));

        /* when */
        assertThrows(LoginFailedException.class, () -> {
            authenticationService.login(request);
        }, "아이디 또는 비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("로그아웃을 성공하면 DB에서 리프레쉬토큰을 삭제한다.")
    void 로그아웃을_성공하면_DB에서_리프레쉬토큰을_삭제한다() {
        /* given */
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);

        /* when */
        authenticationService.logout(customUserDetails);

        /* then */
        Mockito.verify(jwtUtils, Mockito.times(1)).deleteRefreshToken(UserFixtures.회원_아이디);
    }
    
    @Test
    @DisplayName("토큰 재발급을 성공하면 유저정보와 새로운 토큰을 반환한다.")
    void 토큰_재발급을_성공하면_유저정보와_새로운_토큰을_반환한다() {
        /* given */
        Mockito.when(jwtUtils.getUserId(AuthenticationFixtures.refreshToken)).thenReturn(UserFixtures.회원_아이디);
        Mockito.when(userRepository.findByUserId(UserFixtures.회원_아이디)).thenReturn(Optional.of(UserFixtures.회원));
        Mockito.when(jwtUtils.generateToken(UserFixtures.회원_아이디, UserFixtures.권한))
               .thenReturn(Map.of("accessToken", AuthenticationFixtures.accessToken, "refreshToken", AuthenticationFixtures.refreshToken));
        
        /* when */
        TokenResponse tokenResponse = authenticationService.tokenReissue(AuthenticationFixtures.토큰_재발급_요청);

        /* then */
        assertEquals(tokenResponse.getAccessToken(), AuthenticationFixtures.accessToken);
        assertEquals(tokenResponse.getRefreshToken(), AuthenticationFixtures.refreshToken);
    }

}
