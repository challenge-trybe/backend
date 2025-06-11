package com.trybe.moduleapi.proof.event.model;

import com.trybe.moduleapi.proof.event.type.ProofEventType;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.user.entity.User;

import java.util.List;

public record ProofEvent(ProofEventType eventType, Proof proof, String challengeTitle, List<User> participants) {
    public static ProofEvent from(ProofEventType eventType, Proof proof, List<User> participants) {
        return new ProofEvent(eventType, proof, proof.getChallenge().getTitle(), participants);
    }
}
