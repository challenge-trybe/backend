package com.trybe.modulecore.post.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Repository
public class PopularPostCache {
    private final RedisTemplate<String, Long> redisTemplate;
    private final PostCreatedAtCache postCreatedAtCache;

    private static final String POPULAR_POST_KEY = "popular:posts:%s";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int POPULAR_POST_COUNT = 10;
    private static final Duration TTL = Duration.ofDays(2);

    public PopularPostCache(RedisTemplate<String, Long> redisTemplate, PostCreatedAtCache postCreatedAtCache) {
        this.redisTemplate = redisTemplate;
        this.postCreatedAtCache = postCreatedAtCache;
    }

    public void update(Long postId, double score) {
        LocalDate createdAt = postCreatedAtCache.getCreatedAtByPostId(postId);
        String popularPostKey = createPopularPostKey(createdAt);

        Double currentScore = redisTemplate.opsForZSet().score(popularPostKey, postId);
        if (currentScore == null) {
            redisTemplate.opsForZSet().add(popularPostKey, postId, score);
            redisTemplate.expire(popularPostKey, TTL);
        } else {
            redisTemplate.opsForZSet().incrementScore(popularPostKey, postId, score);
        }
    }

    public void remove(Long postId){
        LocalDate createdAt = postCreatedAtCache.getCreatedAtByPostId(postId);
        String key = createPopularPostKey(createdAt);
        redisTemplate.opsForZSet().remove(key, postId);
    }

    public Set<Long> findPopularPostIds(LocalDate time){
        String key = createPopularPostKey(time);
        return redisTemplate.opsForZSet().reverseRange(key, 0, POPULAR_POST_COUNT - 1);
    }

    private String createPopularPostKey(LocalDate time){
        String formattedTime = DATE_TIME_FORMATTER.format(time);
        return createPopularPostKey(formattedTime);
    }

    private String createPopularPostKey(String timeStr){
        return String.format(POPULAR_POST_KEY,timeStr);
    }
}
