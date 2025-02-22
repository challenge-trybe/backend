package com.trybe.moduleapi.user.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.token.request.RefreshTokenRequest;
import com.trybe.moduleapi.token.response.TokenResponse;
import com.trybe.moduleapi.user.dto.request.LoginRequest;
import com.trybe.moduleapi.user.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest loginRequest){
        return authenticationService.login(loginRequest);
    }

    @PostMapping("/logout")
    public void logout(@AuthenticationPrincipal CustomUserDetails userDetails){
        authenticationService.logout(userDetails.getUser());
    }

    @PostMapping("/token/reissue")
    public TokenResponse tokenReissue(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authenticationService.tokenReissue(refreshTokenRequest);
    }
}
