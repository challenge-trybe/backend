package com.trybe.modulecore.challenge.repository.popular;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Repository
public class PopularChallengeRedisCache implements PopularChallengeCache {
    private final RedisTemplate<String, Long> redisTemplate;

    public PopularChallengeRedisCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static final String POPULAR_CHALLENGES_KEY = "popular:challenges:%s";
    private static final Duration POPULAR_CHALLENGES_DURATION = Duration.ofDays(2);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public void increaseScore(Long challengeId, int score, LocalDate date) {
        String formattedDate = getFormattedDate(date);
        String key = getRedisKey(POPULAR_CHALLENGES_KEY, formattedDate);

        redisTemplate.opsForZSet().incrementScore(key, challengeId, score);
        redisTemplate.expire(key, POPULAR_CHALLENGES_DURATION);
    }

    @Override
    public void decreaseScore(Long challengeId, int score, LocalDate date) {
        String formattedDate = getFormattedDate(date);
        String key = getRedisKey(POPULAR_CHALLENGES_KEY, formattedDate);

        redisTemplate.opsForZSet().incrementScore(key, challengeId, -score);
    }

    @Override
    public Set<Long> getTopPopularChallenges(LocalDate date, int count) {
        String formattedDate = getFormattedDate(date);
        String key = getRedisKey(POPULAR_CHALLENGES_KEY, formattedDate);

        return Optional.ofNullable(redisTemplate.opsForZSet().reverseRange(key, 0, count - 1))
                .orElse(Collections.emptySet());
    }

    private String getRedisKey(String key, String date) {
        return String.format(key, date);
    }

    private String getFormattedDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
