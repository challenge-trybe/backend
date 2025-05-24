package com.trybe.moduleapi.user.fixtures;

import com.trybe.moduleapi.file.dto.FileResponse;
import com.trybe.moduleapi.user.dto.request.UserRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.file.entity.File;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Gender;
import com.trybe.modulecore.user.enums.Role;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDate;
import java.util.List;

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

    public static final String 프로필_기본_경로 = "profile/user";
    public static final String 프로필_이미지_URL = "https://fake-s3-bucket.s3.amazonaws.com/profile/user/saved-test-image.jpg";

    public static final FileResponse 파일_응답 = FileResponse.from("originalName.jpg", 프로필_이미지_URL);

    public static final MockMultipartFile 프로필_이미지_요청 = new MockMultipartFile(
            "file",
            "new-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
    );

    public static final File 프로필_이미지_파일 = File.builder()
            .originalName("default-image.jpg")
            .savedName("UUID.jpg")
            .filePath(프로필_기본_경로 + "/default-image.jpg")
            .fileType("image/jpeg")
            .fileSize(1024L)
            .build();

    public static final File 수정된_프로필_이미지_파일 = File.builder()
            .originalName("new-image.jpg")
            .savedName("UUID.jpg")
            .filePath(프로필_기본_경로 + "/update-test-image.jpg")
            .fileType("image/jpeg")
            .fileSize(1024L)
            .build();

    public static User 회원 = 회원_생성(회원_아이디, 회원_이메일);
    public static User 회원() {
        return 회원_생성(회원_아이디, 회원_이메일);
    }

    public static User 회원_생성(String userId, String email) {
        User user = User.builder()
                .userId(userId)
                .email(email)
                .nickname(회원_닉네임)
                .gender(회원_성별)
                .birth(회원_생년월일)
                .build();
        user.updateProfileImage(프로필_이미지_파일);
        return user;
    }

    public static UserRequest.Create 회원가입_요청 = new UserRequest.Create(회원_아이디,회원_이메일,회원_닉네임,회원_비밀번호,회원_성별,회원_생년월일);
    public static UserRequest.Create 잘못된_회원가입_요청 = new UserRequest.Create(잘못된_회원_아이디,잘못된_회원_이메일,잘못된_회원_닉네임,잘못된_회원_비밀번호,잘못된_회원_성별,잘못된_회원_생년월일);
    public static UserRequest.Update 회원정보_수정_요청 = new UserRequest.Update(수정된_회원_닉네임,수정된_회원_이메일,수정된_회원_성별,수정된_회원_생년월일);
    public static UserRequest.Update 잘못된_회원정보_수정_요청 = new UserRequest.Update(잘못된_회원_닉네임,잘못된_회원_이메일,잘못된_회원_성별,잘못된_회원_생년월일);
    public static UserResponse.Detail 회원_응답 = new UserResponse.Detail(회원_PK,회원_닉네임,회원_아이디,회원_이메일,회원_성별,회원_생년월일, 파일_응답);
    public static UserResponse.Detail 수정된_회원_응답 = new UserResponse.Detail(회원_PK,수정된_회원_닉네임,회원_아이디,수정된_회원_이메일,수정된_회원_성별,수정된_회원_생년월일, 파일_응답);
    public static UserResponse.Summary 요약_회원_응답 = new UserResponse.Summary(회원_PK,회원_아이디,회원_닉네임);
    public static UserRequest.UpdatePassword 비밀번호_변경_요청 = new UserRequest.UpdatePassword(현재_비밀번호,새로운_비밀번호,확인_비밀번호);
    public static UserRequest.UpdatePassword 현재_새로운_비밀번호_동일한_비밀번호_변경_요청 = new UserRequest.UpdatePassword(현재_비밀번호,현재_비밀번호,확인_비밀번호);
    public static UserRequest.UpdatePassword 현재_비밀번호_잘못된_비밀번호_변경_요청 = new UserRequest.UpdatePassword(새로운_비밀번호,새로운_비밀번호,확인_비밀번호);
    public static UserRequest.UpdatePassword 새로운_확인용_비밀번호_다른_변경_요청 = new UserRequest.UpdatePassword(현재_비밀번호,새로운_비밀번호,현재_비밀번호);

    public static List<User> 채팅_오프라인_유저 = List.of(회원_생성("test1" , "test1@test.com"),
                                               회원_생성("test2" , "test2@test.com"),
                                               회원_생성("test3" , "test3@test.com"),
                                               회원_생성("test4" , "test4@test.com"));
}
