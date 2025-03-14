package com.trybe.modulecore.proof.repository;

import com.trybe.modulecore.proof.entity.ProofHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProofHistoryRepository extends JpaRepository<ProofHistory, Long> {
    Page<ProofHistory> findAllByProofId(Long proofId, Pageable pageable);
}
