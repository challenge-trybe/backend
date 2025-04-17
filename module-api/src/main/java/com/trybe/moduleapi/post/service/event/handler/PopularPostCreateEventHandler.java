package com.trybe.moduleapi.post.service.event.handler;

import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.modulecore.post.repository.PostCreatedAtCache;
import org.springframework.stereotype.Component;

@Component
public class PopularPostCreateEventHandler implements PostEventHandler {
    private final PostCreatedAtCache postCreatedAtCache;

    public PopularPostCreateEventHandler(PostCreatedAtCache postCreatedAtCache) {
        this.postCreatedAtCache = postCreatedAtCache;
    }

    @Override
    public boolean supports(PostEventType type) {
        return type == PostEventType.POST_CREATED;
    }

    @Override
    public void handle(PostEvent event) {
        postCreatedAtCache.add(event.postId());
    }
}
