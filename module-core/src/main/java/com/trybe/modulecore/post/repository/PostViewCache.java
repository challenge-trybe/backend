package com.trybe.modulecore.post.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class PostViewCache {
    private final RedisTemplate<String, Long> redisTemplate;

    public PostViewCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String USER_VIEWED_POST_KEY = "user:%d:view:post:%d";
    private static final Duration TTL = Duration.ofHours(3);

    public boolean hasViewed(Long userId, Long postId) {
        String viewKey = createRedisKey(userId, postId);
        return redisTemplate.hasKey(viewKey);
    }

    public void recordView(Long userId, Long postId) {
        String key = createRedisKey(userId, postId);
        redisTemplate.opsForValue().set(key, 1L, TTL);
    }

    private String createRedisKey(Long userId, Long postId){
        return String.format(USER_VIEWED_POST_KEY, userId, postId);
    }
}
