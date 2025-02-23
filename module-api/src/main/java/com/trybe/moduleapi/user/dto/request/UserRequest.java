package com.trybe.moduleapi.user.dto.request;

import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Gender;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UserRequest {
    private static final String USERID_VALUE = "아이디를 입력해주세요.";
    private static final int MIN_USERID_LENGTH = 6;
    private static final int MAX_USERID_LENGTH = 12;
    private static final String USERID_LENGTH = "아이디는 6자~12자 사이로 입력해주세요.";

    private static final String EMAIL_VALUE = "이메일을 입력해주세요.";
    private static final String EMAIL_FORMAT = "이메일 형식이 다릅니다.";

    private static final String NICKNAME_VALUE = "닉네임을 입력해주세요.";
    private static final String NICKNAME_REGEXP = "^(?!\\d+$)[a-zA-Z0-9가-힣]+$";
    private static final String NICKNAME_FORMAT = "영문자, 숫자, 한글만 사용 가능하고, 1자 이상이어야 합니다. 특수문자나 공백은 허용되지 않습니다.";
    private static final int MIN_NICKNAME_LENGTH = 2;
    private static final int MAX_NICKNAME_LENGTH = 8;
    private static final String NICKNAME_LENGTH = "닉네임은 2자~8자리 이내로 입력해주세요.";

    private static final String PASSWORD_VALUE = "비밀번호를 입력해주세요.";
    private static final String PASSWORD_REGEXP = "[a-zA-Z1-9]{6,12}";
    private static final String PASSWORD_FORMAT = "비밀번호는 영어와 숫자를 포함해서 6~12자리 이내로 입력해주세요.";

    private static final String GENDER_VALUE = "성별을 선택해주세요.";
    private static final String BIRTH_VALUE = "생년월일 형식이 다릅니다.";

    public record Create(
            @NotBlank(message = USERID_VALUE)
            @Size(min = MIN_USERID_LENGTH, max = MAX_USERID_LENGTH, message = USERID_LENGTH)
            String userId,

            @NotBlank(message = EMAIL_VALUE)
            @Email(message = EMAIL_FORMAT)
            String email,

            @NotBlank(message = NICKNAME_VALUE)
            @Pattern(regexp=NICKNAME_REGEXP, message = NICKNAME_FORMAT)
            @Size(min = MIN_NICKNAME_LENGTH, max = MAX_NICKNAME_LENGTH, message = NICKNAME_LENGTH)
            String nickname,

            @NotBlank(message = PASSWORD_VALUE)
            @Pattern(regexp=PASSWORD_REGEXP, message = PASSWORD_FORMAT)
            String password,

            @NotNull(message = GENDER_VALUE)
            Gender gender,

            @NotNull(message = BIRTH_VALUE)
            LocalDate birth
    ){
        public User toEntity(){
            return User.builder()
                    .userId(userId())
                    .nickname(nickname())
                    .email(email())
                    .gender(gender())
                    .birth(birth())
                    .build();
        }
    }
    public record Update(
            @NotBlank(message = NICKNAME_VALUE)
            @Pattern(regexp=NICKNAME_REGEXP, message = NICKNAME_FORMAT)
            @Size(min = MIN_NICKNAME_LENGTH, max = MAX_NICKNAME_LENGTH, message = NICKNAME_LENGTH)
            String nickname,

            @Email(message = EMAIL_FORMAT)
            String email,

            @NotNull(message = GENDER_VALUE)
            Gender gender,

            @NotNull(message = BIRTH_VALUE)
            LocalDate birth
    ){}

    public record UpdatePassword(
            @NotBlank(message = PASSWORD_VALUE)
            String oldPassword,

            @NotBlank(message = PASSWORD_VALUE)
            @Pattern(regexp=PASSWORD_REGEXP, message = PASSWORD_FORMAT)
            String newPassword,

            @NotBlank(message = PASSWORD_VALUE)
            String confirmPassword
    ){}
}
