package com.trybe.moduleapi.challenge.event.pub;

import com.trybe.moduleapi.challenge.event.model.ChallengeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ChallengeApplicationEventPublisher implements ChallengeEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public ChallengeApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(ChallengeEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
