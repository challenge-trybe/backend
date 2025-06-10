package com.trybe.moduleapi.proof.event.pub;

import com.trybe.moduleapi.proof.event.model.ProofEvent;
import com.trybe.moduleapi.proof.event.model.ProofHistoryEvent;

public interface ProofEventPublisher {
    void publish(ProofEvent event);
    void publish(ProofHistoryEvent event);
}
