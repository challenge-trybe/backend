package com.trybe.moduleapi.post.service.event.handler;

import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.modulecore.post.repository.PopularPostCache;
import org.springframework.stereotype.Component;

@Component
public class CommentDeleteEventHandler implements PostEventHandler {
    private final PopularPostCache popularPostCache;

    public CommentDeleteEventHandler(PopularPostCache popularPostCache) {
        this.popularPostCache = popularPostCache;
    }

    @Override
    public boolean supports(PostEventType type) {
        return type == PostEventType.COMMENT_DELETED;
    }

    @Override
    public void handle(PostEvent event) {
        popularPostCache.update(event.postId(), event.eventType().getScore());

    }
}
