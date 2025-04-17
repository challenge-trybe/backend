package com.trybe.moduleapi.post.service.event.handler;

import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.modulecore.post.repository.PopularPostCache;
import com.trybe.modulecore.post.repository.PostCreatedAtCache;
import org.springframework.stereotype.Component;

@Component
public class PopularPostDeleteEventHandler implements PostEventHandler {
    private final PostCreatedAtCache postCreatedAtCache;
    private final PopularPostCache popularPostCache;

    public PopularPostDeleteEventHandler(PostCreatedAtCache postCreatedAtCache, PopularPostCache popularPostCache) {
        this.postCreatedAtCache = postCreatedAtCache;
        this.popularPostCache = popularPostCache;
    }

    @Override
    public boolean supports(PostEventType type) {
        return type == PostEventType.POST_DELETED;
    }

    @Override
    public void handle(PostEvent event) {
        postCreatedAtCache.remove(event.postId());
        popularPostCache.remove(event.postId());
    }
}
