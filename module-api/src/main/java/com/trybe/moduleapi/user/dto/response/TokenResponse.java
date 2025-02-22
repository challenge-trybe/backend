package com.trybe.moduleapi.user.dto.response;

import lombok.Getter;

public record TokenResponse(
        String grantType,
        String accessToken,
        String refreshToken

) {
    // 음..
}
