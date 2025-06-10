package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.proof.event.model.ProofEvent;
import com.trybe.moduleapi.proof.event.pub.ProofEventPublisher;
import com.trybe.moduleapi.proof.event.type.ProofEventType;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.repository.ProofRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProofScheduler {
    private final ProofRepository proofRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ProofEventPublisher proofEventPublisher;

    public ProofScheduler(ProofRepository proofRepository, ChallengeParticipationRepository challengeParticipationRepository, ProofEventPublisher proofEventPublisher) {
        this.proofRepository = proofRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.proofEventPublisher = proofEventPublisher;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void notifyProofStart() {
        LocalDate today = LocalDate.now();
        List<Proof> proofs = proofRepository.findAllByDate(today);

        for (Proof proof : proofs) {
            List<User> participants = getParticipants(proof.getChallenge().getId());
            proofEventPublisher.publish(new ProofEvent(proof, ProofEventType.START, participants));
        }
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void notifyProofEnd() {
        LocalDate today = LocalDate.now();
        List<Proof> proofs = proofRepository.findAllByDate(today);

        for (Proof proof : proofs) {
            List<User> participants = getParticipants(proof.getChallenge().getId());
            proofEventPublisher.publish(new ProofEvent(proof, ProofEventType.END, participants));
        }
    }

    private List<User> getParticipants(Long challengeId) {
        return challengeParticipationRepository.findAllByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED)
                .stream()
                .map(ChallengeParticipation::getUser)
                .toList();
    }
}