package com.trybe.modulecore.challenge.repository.view;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class ChallengeViewRedisCache implements ChallengeViewCache {
    private final RedisTemplate<String, Long> redisTemplate;

    public ChallengeViewRedisCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final String USER_VIEWED_CHALLENGE_KEY = "user:%d:viewed:challenge:%d";

    @Override
    public boolean hasViewed(Long userId, Long challengeId) {
        String key = String.format(USER_VIEWED_CHALLENGE_KEY, userId, challengeId);
        return redisTemplate.hasKey(key);
    }

    @Override
    public void recordView(Long userId, Long challengeId) {
        String key = String.format(USER_VIEWED_CHALLENGE_KEY, userId, challengeId);
        redisTemplate.opsForValue().set(key, 1L, Duration.ofHours(3));
    }
}
