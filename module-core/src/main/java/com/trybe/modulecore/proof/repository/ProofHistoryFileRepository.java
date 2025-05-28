package com.trybe.modulecore.proof.repository;

import com.trybe.modulecore.proof.entity.ProofHistoryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProofHistoryFileRepository extends JpaRepository<ProofHistoryFile, Long> {
    List<ProofHistoryFile> findAllByProofHistoryIdOrderByFileOrder(Long proofHistoryId);
}
