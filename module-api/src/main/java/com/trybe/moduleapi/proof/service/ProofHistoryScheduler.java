package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.proof.event.model.ProofHistoryEvent;
import com.trybe.moduleapi.proof.event.pub.ProofEventPublisher;
import com.trybe.moduleapi.proof.event.type.ProofHistoryEventType;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.proof.repository.vote.ProofHistoryVoteCache;
import com.trybe.modulecore.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProofHistoryScheduler {
    private final ProofHistoryRepository proofHistoryRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ProofHistoryVoteCache proofHistoryVoteCache;
    private final ProofEventPublisher proofEventPublisher;

    public ProofHistoryScheduler(ProofHistoryRepository proofHistoryRepository, ChallengeParticipationRepository challengeParticipationRepository, ProofHistoryVoteCache proofHistoryVoteCache, ProofEventPublisher proofEventPublisher) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.proofHistoryVoteCache = proofHistoryVoteCache;
        this.proofEventPublisher = proofEventPublisher;
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateProofHistoryStatusByVote() {
        LocalDate targetDate = LocalDate.now().minusDays(2);
        List<ProofHistory> proofHistories = proofHistoryRepository.findAllByProof_Date(targetDate);

        proofHistories.forEach(proofHistory -> {
            boolean result = isApproved(proofHistory);
            proofHistory.updateStatus(result ? ProofHistoryStatus.PASSED : ProofHistoryStatus.FAILED);

            List<User> participants = getParticipants(proofHistory.getProof().getChallenge().getId());
            proofEventPublisher.publish(ProofHistoryEvent.from(ProofHistoryEventType.VOTE_END, proofHistory, participants));
        });
    }

    private boolean isApproved(ProofHistory proofHistory) {
        int approvedCount = proofHistoryVoteCache.getVoteCount(proofHistory.getId(), true);
        int disapprovedCount = proofHistoryVoteCache.getVoteCount(proofHistory.getId(), false);

        return approvedCount >= disapprovedCount;
    }

    private List<User> getParticipants(Long challengeId) {
        return challengeParticipationRepository.findAllByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED)
                .stream()
                .map(ChallengeParticipation::getUser)
                .peek(User::getUuid)
                .toList();
    }
}
