package com.trybe.moduleapi.challenge.event.pub;

import com.trybe.moduleapi.challenge.event.ChallengeEvent;

public interface ChallengeEventPublisher {
    void publish(ChallengeEvent event);
}
