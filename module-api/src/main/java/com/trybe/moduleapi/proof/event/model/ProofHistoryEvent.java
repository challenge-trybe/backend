package com.trybe.moduleapi.proof.event.model;

import com.trybe.moduleapi.proof.event.type.ProofHistoryEventType;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.user.entity.User;
import lombok.Getter;

import java.util.List;

@Getter
public class ProofHistoryEvent {
    private final ProofHistory proofHistory;
    private final ProofHistoryEventType eventType;
    private final List<User> participants;

    public ProofHistoryEvent(ProofHistory proofHistory, ProofHistoryEventType eventType, List<User> participants) {
        this.proofHistory = proofHistory;
        this.eventType = eventType;
        this.participants = participants;
    }
}
