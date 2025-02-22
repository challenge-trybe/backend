package com.trybe.moduleapi.user.dto.response;

import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Gender;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String userId,
        String email,
        Gender gender,
        LocalDate birth
){
    public static UserResponse from(User user){
        return new UserResponse(user.getId(), user.getUserId(), user.getEmail(), user.getGender(), user.getBirth());
    }
}
