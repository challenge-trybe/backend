package com.trybe.moduleapi.challenge.event.pub;

import com.trybe.moduleapi.challenge.event.model.ChallengeEvent;

public interface ChallengeEventPublisher {
    void publish(ChallengeEvent event);
}
