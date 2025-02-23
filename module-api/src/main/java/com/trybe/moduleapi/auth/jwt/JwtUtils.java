package com.trybe.moduleapi.auth.jwt;

import com.trybe.modulecore.token.repository.RefreshTokenRepository;
import com.trybe.modulecore.token.entity.RefreshToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
public class JwtUtils {

    private final Key secretKey;
    private static final long accessTokenExpiredTime = 3600000; // 1시간
    private static final long refreshTokenExpiredTime = 604_800_000; // 7일

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtUtils(@Value("${jwt.secret-key}") String secretKey, RefreshTokenRepository refreshTokenRepository) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public Map<String, String> generateToken(String userId, String role){
        String accessToken = createToken(userId, role, accessTokenExpiredTime);
        String refreshToken = createToken(userId, role, refreshTokenExpiredTime);

        Optional<RefreshToken> existRefreshToken = refreshTokenRepository.findByUserId(userId);
        if (existRefreshToken.isPresent()) {
            existRefreshToken.get().updateRefreshToken(refreshToken);
        } else {
            RefreshToken refreshTokenEntity = RefreshToken.builder()
                                                          .userId(userId)
                                                          .refreshToken(refreshToken)
                                                          .build();
            refreshTokenRepository.save(refreshTokenEntity);
        }
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    private String createToken(String userId, String role, long expiredTime){
        return Jwts.builder()
                   .setHeaderParam("typ", "jwt")
                   .setSubject(userId)
                   .claim("role", role)
                   .setIssuedAt(new Date())
                   .setExpiration(new Date(System.currentTimeMillis() + expiredTime))
                   .signWith(secretKey)
                   .compact();
    }

    public String getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public void deleteRefreshToken(String userId){
        refreshTokenRepository.deleteByUserId(userId);
    }


    public boolean validateToken(String accessToken){
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(accessToken);
            return true;
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    private Claims parseToken(String token){
        return Jwts.parserBuilder()
                   .setSigningKey(secretKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody();
    }
}
