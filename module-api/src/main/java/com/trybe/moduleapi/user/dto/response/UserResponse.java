package com.trybe.moduleapi.user.dto.response;

import com.trybe.moduleapi.file.dto.FileResponse;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Gender;

import java.time.LocalDate;

public class UserResponse {

    public record Detail(
            Long id,
            String nickname,
            String userId,
            String email,
            Gender gender,
            LocalDate birth,
            FileResponse fileResponse
    ) {
        public static Detail from(User user, FileResponse fileResponse) {
            return new Detail(
                    user.getId(),
                    user.getNickname(),
                    user.getUserId(),
                    user.getEmail(),
                    user.getGender(),
                    user.getBirth(),
                    fileResponse
            );
        }
    }

    public record Summary(
            Long id,
            String userId,
            String nickname
    ){
        public static Summary from(User user) {
            return new Summary(
                    user.getId(),
                    user.getUserId(),
                    user.getNickname()
            );
        }
    }
}

