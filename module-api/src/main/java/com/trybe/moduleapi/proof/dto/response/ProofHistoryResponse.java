package com.trybe.moduleapi.proof.dto.response;

import com.trybe.moduleapi.file.dto.FileWithIdResponse;
import com.trybe.moduleapi.user.dto.response.UserResponse;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;

import java.time.LocalDateTime;
import java.util.List;

public class ProofHistoryResponse {
    public record Summary(
            Long id,
            UserResponse.Summary writer,
            String content,
            List<FileWithIdResponse> files,
            ProofHistoryStatus status,
            LocalDateTime createdAt
    ) {
        public static Summary from(ProofHistory proofHistory, List<FileWithIdResponse> files) {
            return new Summary(
                    proofHistory.getId(),
                    UserResponse.Summary.from(proofHistory.getUser()),
                    proofHistory.getContent(),
                    files,
                    proofHistory.getStatus(),
                    proofHistory.getCreatedAt()
            );
        }
    }

    public record Detail(
            ProofResponse.Summary proof,
            Long id,
            UserResponse.Summary writer,
            String content,
            List<FileWithIdResponse> files,
            ProofHistoryStatus status,
            LocalDateTime createdAt
    ) {
        public static Detail from(ProofHistory proofHistory, List<FileWithIdResponse> files) {
            return new Detail(
                    ProofResponse.Summary.from(proofHistory.getProof()),
                    proofHistory.getId(),
                    UserResponse.Summary.from(proofHistory.getUser()),
                    proofHistory.getContent(),
                    files,
                    proofHistory.getStatus(),
                    proofHistory.getCreatedAt()
            );
        }
    }
}
