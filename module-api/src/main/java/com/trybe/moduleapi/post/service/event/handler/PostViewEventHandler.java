package com.trybe.moduleapi.post.service.event.handler;

import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.modulecore.post.repository.PopularPostCache;
import org.springframework.stereotype.Component;

@Component
public class PostViewEventHandler implements PostEventHandler{
    private final PopularPostCache popularPostCache;

    public PostViewEventHandler(PopularPostCache popularPostCache) {
        this.popularPostCache = popularPostCache;
    }

    @Override
    public boolean supports(PostEventType type) {
        return type == PostEventType.POST_VIEW;
    }

    @Override
    public void handle(PostEvent event) {
        popularPostCache.update(event.postId(), event.eventType().getScore());
    }
}
