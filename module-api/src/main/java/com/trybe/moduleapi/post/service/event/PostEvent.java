package com.trybe.moduleapi.post.service.event;

public record PostEvent(
        Long postId,
        PostEventType eventType
){
    public static PostEvent from(Long postId, PostEventType eventType){
        return new PostEvent(postId, eventType);
    }
}
