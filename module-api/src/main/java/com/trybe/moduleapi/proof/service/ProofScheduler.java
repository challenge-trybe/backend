package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.proof.event.model.ProofEvent;
import com.trybe.moduleapi.proof.event.pub.ProofEventPublisher;
import com.trybe.moduleapi.proof.event.type.ProofEventType;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.repository.ProofRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProofScheduler {
    private final ProofRepository proofRepository;
    private final ProofEventPublisher proofEventPublisher;

    public ProofScheduler(ProofRepository proofRepository, ProofEventPublisher proofEventPublisher) {
        this.proofRepository = proofRepository;
        this.proofEventPublisher = proofEventPublisher;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void notifyProofStart() {
        LocalDate today = LocalDate.now();
        List<Proof> proofs = proofRepository.findAllByDate(today);

        for (Proof proof : proofs) {
            proofEventPublisher.publish(new ProofEvent(proof, ProofEventType.START));
        }
    }

    @Scheduled(cron = "0 59 23 * * *")
    public void notifyProofEnd() {
        LocalDate today = LocalDate.now();
        List<Proof> proofs = proofRepository.findAllByDate(today);

        for (Proof proof : proofs) {
            proofEventPublisher.publish(new ProofEvent(proof, ProofEventType.END));
        }
    }
}