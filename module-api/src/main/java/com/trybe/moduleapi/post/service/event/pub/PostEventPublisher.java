package com.trybe.moduleapi.post.service.event.pub;

import com.trybe.moduleapi.post.service.event.PostEvent;

public interface PostEventPublisher {
    void publish(PostEvent event);
}
