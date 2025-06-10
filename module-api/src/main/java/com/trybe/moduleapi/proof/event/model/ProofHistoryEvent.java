package com.trybe.moduleapi.proof.event.model;

import com.trybe.moduleapi.proof.event.type.ProofHistoryEventType;
import com.trybe.modulecore.proof.entity.ProofHistory;
import lombok.Getter;

@Getter
public class ProofHistoryEvent {
    private final ProofHistory proofHistory;
    private final ProofHistoryEventType eventType;

    public ProofHistoryEvent(ProofHistory proofHistory, ProofHistoryEventType eventType) {
        this.proofHistory = proofHistory;
        this.eventType = eventType;
    }
}
