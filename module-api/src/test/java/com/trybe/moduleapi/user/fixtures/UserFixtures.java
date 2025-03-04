package com.trybe.moduleapi.user.fixtures;

import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Gender;
import com.trybe.modulecore.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public class UserFixtures {
    public static final Long 회원_PK = 1L;
    public static final String 회원_아이디 = "rnignon";
    public static final String 회원_이메일 = "rnignon@test.com";
    public static final String 회원_닉네임 = "누운토끼는눔바니";
    public static final String 회원_비밀번호 = "testpwd123";
    public static final String 회원_암호화된_비밀번호 = "encodedPassword";
    public static final Gender 회원_성별 = Gender.FEMALE;
    public static final LocalDate 회원_생년월일 = LocalDate.of(2001,11,14);

    public static final String 수정된_회원_이메일 = "jiwon@test.com";
    public static final String 수정된_회원_닉네임 = "닥터전자레인지";
    public static final String 수정된_회원_비밀번호 = "change123";
    public static final String 수정된_회원_암호화된_비밀번호 = "encodedPassword";
    public static final Gender 수정된_회원_성별 = Gender.MALE;
    public static final LocalDate 수정된_회원_생년월일 = LocalDate.of(2001,5,12);

    public static final String 잘못된_회원_아이디 = "";
    public static final String 잘못된_회원_이메일 = "rnignon#test.com";
    public static final String 잘못된_회원_닉네임 = "1";
    public static final String 잘못된_회원_비밀번호 = "1";
    public static final Gender 잘못된_회원_성별 = null;
    public static final LocalDate 잘못된_회원_생년월일 = null;

    public static final String 현재_비밀번호 = 회원_비밀번호;
    public static final String 새로운_비밀번호 = "newPassword";
    public static final String 새로운_암호화된_비밀번호 = "newEncodePwd";
    public static final String 확인_비밀번호 = "newPassword";

    public static final String 권한 = Role.ROLE_USER.getDescription();

    public static User 회원 = User.builder()
                                .userId(회원_아이디)
                                .email(회원_이메일)
                                .nickname(회원_닉네임)
                                .gender(회원_성별)
                                .birth(회원_생년월일)
                                .build();

    public static User 회원_생성(String userId, String email) {
        return User.builder()
                .userId(userId)
                .email(email)
                .nickname(회원_닉네임)
                .gender(회원_성별)
                .birth(회원_생년월일)
                .build();
    }

    public static UserRequest.Create 회원가입_요청 = new UserRequest.Create(회원_아이디,회원_이메일,회원_닉네임,회원_비밀번호,회원_성별,회원_생년월일);
    public static UserRequest.Create 잘못된_회원가입_요청 = new UserRequest.Create(잘못된_회원_아이디,잘못된_회원_이메일,잘못된_회원_닉네임,잘못된_회원_비밀번호,잘못된_회원_성별,잘못된_회원_생년월일);
    public static UserRequest.Update 회원정보_수정_요청 = new UserRequest.Update(수정된_회원_닉네임,수정된_회원_이메일,수정된_회원_성별,수정된_회원_생년월일);
    public static UserRequest.Update 잘못된_회원정보_수정_요청 = new UserRequest.Update(잘못된_회원_닉네임,잘못된_회원_이메일,잘못된_회원_성별,잘못된_회원_생년월일);
    public static UserResponse 회원_응답 = new UserResponse(회원_PK,회원_닉네임,회원_아이디,회원_이메일,회원_성별,회원_생년월일);
    public static UserResponse 수정된_회원_응답 = new UserResponse(회원_PK,수정된_회원_닉네임,회원_아이디,수정된_회원_이메일,수정된_회원_성별,수정된_회원_생년월일);
    public static UserRequest.UpdatePassword 비밀번호_변경_요청 = new UserRequest.UpdatePassword(현재_비밀번호,새로운_비밀번호,확인_비밀번호);
    public static UserRequest.UpdatePassword 현재_새로운_비밀번호_동일한_비밀번호_변경_요청 = new UserRequest.UpdatePassword(현재_비밀번호,현재_비밀번호,확인_비밀번호);
    public static UserRequest.UpdatePassword 현재_비밀번호_잘못된_비밀번호_변경_요청 = new UserRequest.UpdatePassword(새로운_비밀번호,새로운_비밀번호,확인_비밀번호);
    public static UserRequest.UpdatePassword 새로운_확인용_비밀번호_다른_변경_요청 = new UserRequest.UpdatePassword(현재_비밀번호,새로운_비밀번호,현재_비밀번호);
}
