package com.trybe.modulecore.proof.repository;

import com.trybe.modulecore.proof.entity.Proof;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProofRepository extends JpaRepository<Proof, Long> {
    boolean existsByChallengeIdAndDate(Long challengeId, LocalDate date);
    int countByChallengeId(Long challengeId);
    Page<Proof> findAllByChallengeId(Long challengeId, Pageable pageable);
    List<Proof> findAllByDate(LocalDate date);
}
