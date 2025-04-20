package com.trybe.moduleapi.post.service.event.pub;

import com.trybe.moduleapi.post.service.event.PostEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements PostEventPublisher {
    private final ApplicationEventPublisher publisher;

    public SpringEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void publish(PostEvent event) {
        publisher.publishEvent(event);
    }
}
