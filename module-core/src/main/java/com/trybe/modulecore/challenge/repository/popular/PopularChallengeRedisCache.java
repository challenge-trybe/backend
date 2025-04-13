package com.trybe.modulecore.challenge.repository.popular;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

public class PopularChallengeRedisCache implements PopularChallengeCache {
    private final RedisTemplate<String, Long> redisTemplate;

    public PopularChallengeRedisCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final String POPULAR_CHALLENGES_KEY = "popular:challenges:%s";

    @Override
    public void increaseScore(Long challengeId, int score, LocalDate date) {
        String formattedDate = getFormattedDate(date);
        String key = getRedisKey(POPULAR_CHALLENGES_KEY, formattedDate);

        redisTemplate.opsForZSet().incrementScore(key, challengeId, score);
        redisTemplate.expire(key, Duration.ofDays(2));
    }

    @Override
    public void decreaseScore(Long challengeId, int score, LocalDate date) {
        String formattedDate = getFormattedDate(date);
        String key = getRedisKey(POPULAR_CHALLENGES_KEY, formattedDate);

        redisTemplate.opsForZSet().incrementScore(key, challengeId, -score);
    }

    @Override
    public List<Long> getTopPopularChallenges(LocalDate date, int count) {
        String formattedDate = getFormattedDate(date);
        String key = getRedisKey(POPULAR_CHALLENGES_KEY, formattedDate);

        Set<Long> challengeIds = redisTemplate.opsForZSet().reverseRange(key, 0, count - 1);

        return challengeIds == null
                ? List.of()
                : challengeIds.stream().toList();
    }

    private String getRedisKey(String key, String date) {
        return String.format(key, date);
    }

    private String getFormattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        return date.format(formatter);
    }
}
