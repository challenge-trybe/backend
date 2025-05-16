package com.trybe.modulecore.proof.repository;

import com.trybe.modulecore.proof.entity.ProofHistoryFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProofHistoryFileRepository extends JpaRepository<ProofHistoryFile, Long> {
    List<ProofHistoryFile> findAllByProofHistoryIdOrderByFileOrder(Long proofHistoryId);
}
