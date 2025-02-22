package com.trybe.moduleapi.token.response;

import com.trybe.moduleapi.user.dto.response.UserResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenResponse {
    private UserResponse userResponse;
    private String accessToken;
    private String refreshToken;

    public TokenResponse(UserResponse userResponse, String accessToken, String refreshToken) {
        this.userResponse = userResponse;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenResponse from(UserResponse userResponse, String accessToken, String refreshToken){
        return new TokenResponse(userResponse, accessToken, refreshToken);
    }
}
