package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.PostEventType;
import com.trybe.moduleapi.post.service.event.handler.PostEventHandler;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.PopularPostCache;
import com.trybe.modulecore.post.repository.PostCreatedAtCache;
import com.trybe.modulecore.post.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class PopularPostService {
    private final PostRepository postRepository;
    private final PostCreatedAtCache postCreatedAtCache;
    private final PopularPostCache popularPostCache;
    private final List<PostEventHandler> handlers;

    public PopularPostService(PostRepository postRepository, PostCreatedAtCache postCreatedAtCache, PopularPostCache popularPostCache, List<PostEventHandler> handlers) {
        this.postRepository = postRepository;
        this.postCreatedAtCache = postCreatedAtCache;
        this.popularPostCache = popularPostCache;
        this.handlers = handlers;
    }

    public void handlePopularPostEvent(PostEvent event){
        if (!isPostCreatedToday(event)) {
            return;
        }

        handlers.stream()
                .filter(postEventHandler -> postEventHandler.supports(event.eventType()))
                .findAny()
                .ifPresent(postEventHandler -> postEventHandler.handle(event));
    }

    public List<PostResponse.Summary> findTop10Posts(){
        LocalDate time = LocalDate.now().minusDays(1);
        Set<Long> postIds = popularPostCache.findPopularPostIds(time);
        List<Post> posts = postRepository.findAllByIdIn(postIds);
        return posts.stream().map(PostResponse.Summary::from).toList();
    }

    private boolean isPostCreatedToday(PostEvent event) {
        if (event.eventType() == PostEventType.POST_CREATED) {
            return true;
        }
        LocalDate createdAt = postCreatedAtCache.getCreatedAtByPostId(event.postId());
        return LocalDate.now().equals(createdAt);
    }
}
