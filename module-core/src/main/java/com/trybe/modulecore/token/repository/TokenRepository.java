package com.trybe.modulecore.token.repository;

import com.trybe.modulecore.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUserId(String userId);
    Optional<RefreshToken> findByUserId(String userId);
}
