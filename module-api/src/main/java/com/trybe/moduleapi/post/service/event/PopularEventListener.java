package com.trybe.moduleapi.post.service.event;

import com.trybe.moduleapi.post.service.PopularPostService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PopularEventListener {
    private final PopularPostService popularPostService;

    public PopularEventListener(PopularPostService popularPostService) {
        this.popularPostService = popularPostService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void subscribePostEvent(PostEvent event){
        popularPostService.handlePopularPostEvent(event);
    }
}
