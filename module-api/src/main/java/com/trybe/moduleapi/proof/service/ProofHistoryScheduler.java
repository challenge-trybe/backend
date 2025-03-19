package com.trybe.moduleapi.proof.service;

import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProofHistoryScheduler {
    private final ProofHistoryRepository proofHistoryRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    public ProofHistoryScheduler(ProofHistoryRepository proofHistoryRepository, RedisTemplate<String, Long> redisTemplate) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.redisTemplate = redisTemplate;
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
        String approvedCountKey = "proofHistory:" + proofHistory.getId() + ":votes:approvedCount";
        String disapprovedCountKey = "proofHistory:" + proofHistory.getId() + ":votes:disapprovedCount";

        long approvedCount = Optional.ofNullable(redisTemplate.opsForValue().get(approvedCountKey)).orElse(0L);
        long disapprovedCount = Optional.ofNullable(redisTemplate.opsForValue().get(disapprovedCountKey)).orElse(0L);

        return approvedCount >= disapprovedCount;
    }
}
