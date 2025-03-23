package com.trybe.moduleapi.token.response;

import com.trybe.moduleapi.user.dto.response.UserResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class TokenResponse {
    private UserResponse.Summary userResponse;
    private UUID uuid;
    private String accessToken;
    private String refreshToken;

    public TokenResponse(UserResponse.Summary userResponse, UUID uuid, String accessToken, String refreshToken) {
        this.userResponse = userResponse;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static TokenResponse from(UserResponse.Summary userResponse, UUID uuid, String accessToken, String refreshToken){
        return new TokenResponse(userResponse, uuid, accessToken, refreshToken);
    }
}
