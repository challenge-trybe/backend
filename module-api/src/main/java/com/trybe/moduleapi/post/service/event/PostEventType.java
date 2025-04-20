package com.trybe.moduleapi.post.service.event;

import lombok.Getter;

@Getter
public enum PostEventType {
    POST_LIKED(5.0),
    POST_UNLIKED(-5.0),
    COMMENT_CREATED(3.0),
    COMMENT_DELETED(-3.0),
    POST_CREATED(0),
    POST_DELETED(0);
    /**
     * TODO
     * 조회수 이벤트 타입 추가
     */

    private final double score;

    PostEventType(double score) {
        this.score = score;
    }
}
