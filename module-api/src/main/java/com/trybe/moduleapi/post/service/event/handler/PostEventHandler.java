package com.trybe.moduleapi.post.service.event.handler;

import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;

public interface PostEventHandler {
    boolean supports(PostEventType type);
    void handle(PostEvent event);
}
