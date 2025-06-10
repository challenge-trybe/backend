package com.trybe.moduleapi.proof.event.model;

import com.trybe.moduleapi.proof.event.type.ProofEventType;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ProofEvent {
    private final Proof proof;
    private final ProofEventType eventType;
    private final List<User> participants;

    public ProofEvent(Proof proof, ProofEventType eventType, List<User> participants) {
        this.proof = proof;
        this.eventType = eventType;
        this.participants = participants;
    }
}
