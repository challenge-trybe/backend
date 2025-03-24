package com.trybe.modulecore.proof.repository;

import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProofHistoryRepository extends JpaRepository<ProofHistory, Long> {
    Page<ProofHistory> findAllByProofId(Long proofId, Pageable pageable);
    List<ProofHistory> findAllByProof_Date(LocalDate date);
    boolean existsByProofAndUser(Proof proof, User user);
}
