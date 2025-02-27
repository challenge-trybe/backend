package com.trybe.modulecore.challenge.repository;

import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    // 존재하는지
    boolean existsByUserIdAndChallengeId(Long userId, Long challengeId);
    int countByChallengeIdAndStatus(Long challengeId, ParticipationStatus status);
    Page<ChallengeParticipation> findAllByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ParticipationStatus status, Pageable pageable);
    Page<ChallengeParticipation> findAllByChallengeIdAndStatusOrderByCreatedAtAsc(Long challengeId, ParticipationStatus status, Pageable pageable);
    Optional<ChallengeParticipation> findByUserIdAndChallengeId(Long userId, Long challengeId);
}
