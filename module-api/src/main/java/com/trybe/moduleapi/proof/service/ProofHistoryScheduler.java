package com.trybe.moduleapi.proof.service;

import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.proof.repository.vote.ProofHistoryVoteCache;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProofHistoryScheduler {
    private final ProofHistoryRepository proofHistoryRepository;
    private final ProofHistoryVoteCache proofHistoryVoteCache;

    public ProofHistoryScheduler(ProofHistoryRepository proofHistoryRepository, ProofHistoryVoteCache proofHistoryVoteCache) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.proofHistoryVoteCache = proofHistoryVoteCache;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateProofHistoryStatusByVote() {
        LocalDate targetDate = LocalDate.now().minusDays(2);
        List<ProofHistory> proofHistories = proofHistoryRepository.findAllByProof_Date(targetDate);

        proofHistories.forEach(proofHistory -> {
            boolean result = isApproved(proofHistory);
            proofHistory.updateStatus(result ? ProofHistoryStatus.PASSED : ProofHistoryStatus.FAILED);
        });
    }

    private boolean isApproved(ProofHistory proofHistory) {
        int approvedCount = proofHistoryVoteCache.getVoteCount(proofHistory.getId(), true);
        int disapprovedCount = proofHistoryVoteCache.getVoteCount(proofHistory.getId(), false);

        return approvedCount >= disapprovedCount;
    }
}
