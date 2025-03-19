package com.trybe.moduleapi.proof.dto.response;

import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;

import java.time.LocalDateTime;

public class ProofHistoryResponse {
    public record Summary(
            Long id,
            String content,
            ProofHistoryStatus status,
            LocalDateTime createdAt
    ) {
        public static Summary from(ProofHistory proofHistory) {
            return new Summary(
                    proofHistory.getId(),
                    proofHistory.getContent(),
                    proofHistory.getStatus(),
                    proofHistory.getCreatedAt()
            );
        }
    }

    public record Detail(
            ProofResponse.Summary proof,
            Long id,
            String content,
            ProofHistoryStatus status,
            LocalDateTime createdAt
    ) {
        public static Detail from(ProofHistory proofHistory) {
            return new Detail(
                    ProofResponse.Summary.from(proofHistory.getProof()),
                    proofHistory.getId(),
                    proofHistory.getContent(),
                    proofHistory.getStatus(),
                    proofHistory.getCreatedAt()
            );
        }
    }
}
