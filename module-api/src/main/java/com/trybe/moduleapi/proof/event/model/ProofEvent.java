package com.trybe.moduleapi.proof.event.model;

import com.trybe.moduleapi.proof.event.type.ProofEventType;
import com.trybe.modulecore.proof.entity.Proof;
import lombok.Getter;

@Getter
public class ProofEvent {
    private final Proof proof;
    private final ProofEventType eventType;

    public ProofEvent(Proof proof, ProofEventType eventType) {
        this.proof = proof;
        this.eventType = eventType;
    }
}
