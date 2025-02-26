package com.trybe.moduleapi.user.service;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.auth.jwt.JwtUtils;
import com.trybe.moduleapi.token.request.RefreshTokenRequest;
import com.trybe.moduleapi.token.response.TokenResponse;
import com.trybe.moduleapi.user.dto.request.LoginRequest;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.moduleapi.user.exception.LoginFailedException;
import com.trybe.modulecore.user.entity.User;
import com.trybe.modulecore.user.enums.Role;
import com.trybe.modulecore.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthenticationService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public TokenResponse login(LoginRequest loginRequest){
        User user = getUserByUserId(loginRequest.userId());
        checkPassword(loginRequest.password(), user.getEncodedPassword());
        return createTokenResponse(loginRequest.userId());
    }

    @Transactional
    public void logout(CustomUserDetails customUserDetails){
        User user = customUserDetails.getUser();
        jwtUtils.deleteRefreshToken(user.getUserId());
    }

    @Transactional
    public TokenResponse tokenReissue(RefreshTokenRequest refreshTokenRequest){
        String refreshToken = refreshTokenRequest.getRefreshToken();
        String userId = jwtUtils.getUserId(refreshToken);
        return createTokenResponse(userId);
    }

    private void checkPassword(String rawPassword, String encodedPassword){
        if (!passwordEncoder.matches(rawPassword, encodedPassword)){
            throw new LoginFailedException();
        }
    }

    private TokenResponse createTokenResponse(String userId){
        User user = getUserByUserId(userId);
        Role role = user.getRole();
        UserResponse userResponse = UserResponse.from(user);

        Map<String, String> tokenMap = jwtUtils.generateToken(userId, role.getDescription());
        String accessToken = tokenMap.get("accessToken");
        String refreshToken = tokenMap.get("refreshToken");
        return TokenResponse.from(userResponse,accessToken,refreshToken);
    }

    private User getUserByUserId(String userId){
        User user = userRepository.findByUserId(userId).orElseThrow(() -> {
            throw new LoginFailedException();
        });
        return user;
    }
}
