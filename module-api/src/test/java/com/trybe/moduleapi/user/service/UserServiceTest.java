package com.trybe.moduleapi.user.service;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.exception.DuplicatedUserException;
import com.trybe.moduleapi.user.exception.NotFoundUserException;
import com.trybe.moduleapi.user.exception.UpdatePasswordFailException;
import com.trybe.moduleapi.user.fixtures.UserFixtures;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private UserService userService;


    @Test
    @DisplayName("회원가입시 저장된 회원 정보를 반환한다")
    void 회원가입_시_저장된_회원_정보를_반환한다() {
        /* given */
        UserRequest.Create request = UserFixtures.회원가입_요청;
        User user = request.toEntity();
        Mockito.when(userRepository.findByUserId(request.userId())).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any(User.class))).thenReturn(user);
        Mockito.when(passwordEncoder.encode(UserFixtures.회원_비밀번호)).thenReturn(UserFixtures.회원_암호화된_비밀번호);

        /* when */
        UserResponse response = userService.save(request);

        /* then */
        assertEquals(response.userId(), UserFixtures.회원_아이디);
        assertEquals(response.nickname(), UserFixtures.회원_닉네임);
        assertEquals(response.email(), UserFixtures.회원_이메일);
        assertEquals(response.gender(), UserFixtures.회원_성별);
        assertEquals(response.birth(), UserFixtures.회원_생년월일);
    }

    @Test
    @DisplayName("회원가입 중복 아이디 가입시 에러를 반환한다")
    void 회원가입_중복_아이디_가입_시_에러를_반환한다() {
        /* given */
        UserRequest.Create request = UserFixtures.회원가입_요청;
        Mockito.when(userRepository.findByUserId(request.userId())).thenReturn(Optional.of(UserFixtures.회원));

        /* when, then */
        assertThrows(DuplicatedUserException.class, () -> {
            userService.save(request);
        }, "이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("회원가입 중복 이메일 가입시 에러를 반환한다")
    void 회원가입_중복_이메일_가입_시_에러를_반환한다() {
        /* given */
        UserRequest.Create request = UserFixtures.회원가입_요청;
        Mockito.when(userRepository.findByUserId(request.userId())).thenReturn(Optional.of(UserFixtures.회원));

        /* when, then */
        assertThrows(DuplicatedUserException.class, () -> {
            userService.save(request);
        }, "이미 존재하는 이메일입니다.");
    }

    @Test
    @DisplayName("회원조회 시 유저를 반환한다.")
    void 회원조회_시_유저를_반환한다() {
        /* given */
        Mockito.when(userRepository.findById(UserFixtures.회원_PK)).thenReturn(Optional.of(UserFixtures.회원));

        /* when */
        UserResponse response = userService.findById(UserFixtures.회원_PK);

        /* then */
        assertEquals(response.userId(), UserFixtures.회원_아이디);
        assertEquals(response.nickname(), UserFixtures.회원_닉네임);
        assertEquals(response.email(), UserFixtures.회원_이메일);
        assertEquals(response.gender(), UserFixtures.회원_성별);
        assertEquals(response.birth(), UserFixtures.회원_생년월일);
    }

    @Test
    @DisplayName("존재하지 않는 회원조회 시 에러를 반환한다.")
    void 존재하지_않는_회원조회_시_에러를_반환한다() {
        /* given */
        Mockito.when(userRepository.findById(UserFixtures.회원_PK)).thenReturn(Optional.empty());

        /* when, then */
        assertThrows(NotFoundUserException.class, () -> {
            userService.findById(UserFixtures.회원_PK);
        }, "존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("회원가입 수정 성공하면 수정값을 반환한다")
    void 회원가입_수정_성공하면_수정값을_반환한다() {
        /* given */
        UserRequest.Update request = UserFixtures.회원정보_수정_요청;
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);
        UserFixtures.회원.updateProfile(UserFixtures.수정된_회원_닉네임,
                           UserFixtures.수정된_회원_이메일,
                           UserFixtures.수정된_회원_성별,
                           UserFixtures.수정된_회원_생년월일);

        /* when */
        UserResponse response = userService.updateProfile(customUserDetails, request);

        /* then */
        assertEquals(response.nickname(), UserFixtures.수정된_회원_닉네임);
        assertEquals(response.email(), UserFixtures.수정된_회원_이메일);
        assertEquals(response.gender(), UserFixtures.수정된_회원_성별);
        assertEquals(response.birth(), UserFixtures.수정된_회원_생년월일);
    }
    
    @Test
    @DisplayName("정상적인 비밀번호 변경")
    void 정상적인_비밀번호_변경() {
        /* given */
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);
        UserFixtures.회원.updatePassword(UserFixtures.회원_암호화된_비밀번호);
        Mockito.when(passwordEncoder.encode(UserFixtures.새로운_비밀번호)).thenReturn(UserFixtures.새로운_암호화된_비밀번호);
        Mockito.when(passwordEncoder.matches(UserFixtures.회원_비밀번호, UserFixtures.회원_암호화된_비밀번호)).thenReturn(true);

        /* then */
        userService.updatePassword(customUserDetails, UserFixtures.비밀번호_변경_요청);

        /* when */
        Mockito.verify(passwordEncoder, times(1)).encode(UserFixtures.새로운_비밀번호);
    }

    @Test
    @DisplayName("현재 비밀번호와 새로운 비밀번호가 동일하면 에러를 반환한다")
    void 현재_비밀번호와_새로운_비밀번호가_동일하면_에러를_반환한다() {
        /* given */
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);

        /* when, then */
        assertThrows(UpdatePasswordFailException.class, () -> {
            userService.updatePassword(customUserDetails, UserFixtures.현재_새로운_비밀번호_동일한_비밀번호_변경_요청);
        }, "현재 비밀번호와 새로운 비밀번호가 동일합니다.");

    }

    @Test
    @DisplayName("현재 비밀번호를 잘못 입력하면 에러를 반환한다")
    void 현재_비밀번호를_잘못_입력하면_에러를_반환한다() {
        /* given */
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);

        /* when, then */
        assertThrows(UpdatePasswordFailException.class, () -> {
            userService.updatePassword(customUserDetails, UserFixtures.현재_비밀번호_잘못된_비밀번호_변경_요청);
        }, "현재 비밀번호를 잘못 입력하셨습니다.");
    }

    @Test
    @DisplayName("새 비빌번호와 확인 비밀번호가 일치하지 않으면 에러를 반환한다")
    void 새_비빌번호와_확인_비밀번호가_일치하지_않으면_에러를_반환한다() {
        /* given */
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);

        /* when, then */
        assertThrows(UpdatePasswordFailException.class, () -> {
            userService.updatePassword(customUserDetails, UserFixtures.새로운_확인용_비밀번호_다른_변경_요청);
        }, "새 비빌번호와 비밀번호 확인이 일치하지 않습니다.");

    }

    @Test
    @DisplayName("회원탈퇴 성공시 200을 반환한다.")
    void 회원탈퇴_성공_시_200을_반환한다() {
        /* given */
        Mockito.when(customUserDetails.getUser()).thenReturn(UserFixtures.회원);

        /* when */
        userService.delete(customUserDetails);

        /* then */
        Mockito.verify(userRepository, times(1)).deleteById(any());

    }
}
