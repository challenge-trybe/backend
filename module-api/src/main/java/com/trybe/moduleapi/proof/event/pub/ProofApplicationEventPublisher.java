package com.trybe.moduleapi.proof.event.pub;

import com.trybe.moduleapi.proof.event.model.ProofEvent;
import com.trybe.moduleapi.proof.event.model.ProofHistoryEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProofApplicationEventPublisher implements ProofEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public ProofApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(ProofEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(ProofHistoryEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
