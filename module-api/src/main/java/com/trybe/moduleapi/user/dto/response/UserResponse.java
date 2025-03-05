package com.trybe.moduleapi.user.dto.response;

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
            LocalDate birth
    ) {
        public static Detail from(User user) {
            return new Detail(user.getId(),
                              user.getNickname(),
                              user.getUserId(),
                              user.getEmail(),
                              user.getGender(),
                              user.getBirth());
        }
    }

    public record Summary(
            Long id,
            String userId,
            String nickname
    ){
        public static Summary from(User user) {
            return new Summary(user.getId(),
                              user.getUserId(),
                              user.getNickname());
        }
    }
}

