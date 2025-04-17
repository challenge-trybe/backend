package com.trybe.modulecore.post.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.*;

@Repository
public class PostCreatedAtCache {
    private final RedisTemplate<String, Long> redisTemplate;

    private static final String POST_CREATED_KEY = "posts:%d:createdAt";
    private static final Duration TTL = Duration.ofDays(2);

    public PostCreatedAtCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void add(Long postId){
        LocalDate date = LocalDate.now();
        String key = createKey(postId);
        redisTemplate.opsForValue().set(key, date.toEpochDay());
        redisTemplate.expire(key, TTL);
    }

    public LocalDate getCreatedAtByPostId(Long postId){
        String key = createKey(postId);
        Long createdAt = redisTemplate.opsForValue().get(key);
        return LocalDate.ofEpochDay(createdAt);
    }

    public void remove(Long postId){
        String key = createKey(postId);
        redisTemplate.delete(key);
    }

    private String createKey(Long postId){
        return String.format(POST_CREATED_KEY,postId);
    }
}
