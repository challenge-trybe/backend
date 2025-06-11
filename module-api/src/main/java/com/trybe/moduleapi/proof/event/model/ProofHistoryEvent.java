package com.trybe.moduleapi.proof.event.model;

import com.trybe.moduleapi.proof.event.type.ProofHistoryEventType;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.user.entity.User;

import java.util.List;

public record ProofHistoryEvent(ProofHistoryEventType eventType, ProofHistory proofHistory, String challengeTitle, List<User> participants) {
    public static ProofHistoryEvent from(ProofHistoryEventType eventType, ProofHistory proofHistory, List<User> participants) {
        return new ProofHistoryEvent(eventType, proofHistory, proofHistory.getProof().getChallenge().getTitle(), participants);
    }
}
