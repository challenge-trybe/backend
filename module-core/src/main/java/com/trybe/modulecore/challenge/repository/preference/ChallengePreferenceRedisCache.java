package com.trybe.modulecore.challenge.repository.preference;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ChallengePreferenceRedisCache implements ChallengePreferenceCache {
    private final StringRedisTemplate redisTemplate;

    public ChallengePreferenceRedisCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${trybe.challenge.keywords.stopwords}")
    private List<String> stopwords;

    private final String USER_CHALLENGE_KEYWORDS = "user:%d:challenge:keywords";
    private final String USER_CHALLENGE_CATEGORIES = "user:%d:challenge:categories";

    @Override
    public void addPreference(Long userId, Challenge challenge) {
        String category = challenge.getCategory().name();
        List<String> keywords = processKeywords(challenge.getTitle());

        incrementChallengeCategory(userId, category);
        keywords.forEach(keyword -> incrementChallengeKeywords(userId, keyword));
    }

    @Override
    public void removePreference(Long userId, Challenge challenge) {
        String category = challenge.getCategory().name();
        List<String> keywords = processKeywords(challenge.getTitle());

        decrementChallengeCategory(userId, category);
        keywords.forEach(keyword -> decrementChallengeKeywords(userId, keyword));
    }

    @Override
    public void removePreferencesByUser(Long userId) {
        String categoriesKey = getRedisKey(USER_CHALLENGE_CATEGORIES, userId);
        String keywordsKey = getRedisKey(USER_CHALLENGE_KEYWORDS, userId);

        redisTemplate.delete(categoriesKey);
        redisTemplate.delete(keywordsKey);
    }

    @Override
    public List<ChallengeCategory> getPreferenceCategories(Long userId, int count) {
        String key = getRedisKey(USER_CHALLENGE_CATEGORIES, userId);
        Set<String> categories = redisTemplate.opsForZSet().reverseRange(key, 0, count - 1);

        return new ArrayList<>(categories).stream()
                .map(ChallengeCategory::valueOf)
                .toList();
    }

    @Override
    public List<String> getPreferenceKeywords(Long userId, int count) {
        String key = getRedisKey(USER_CHALLENGE_KEYWORDS, userId);
        Set<String> keywords = redisTemplate.opsForZSet().reverseRange(key, 0, count - 1);

        return new ArrayList<>(keywords);
    }

    private void incrementChallengeCategory(Long userId, String category) {
        String key = getRedisKey(USER_CHALLENGE_CATEGORIES, userId);
        redisTemplate.opsForZSet().incrementScore(key, category, 1.0);
    }

    private void decrementChallengeCategory(Long userId, String category) {
        String key = getRedisKey(USER_CHALLENGE_CATEGORIES, userId);
        Double score = redisTemplate.opsForZSet().incrementScore(key, category, -1.0);

        if (score != null && score <= 0) {
            redisTemplate.opsForZSet().remove(key, category);
        }
    }

    private void incrementChallengeKeywords(Long userId, String keyword) {
        String key = getRedisKey(USER_CHALLENGE_KEYWORDS, userId);
        redisTemplate.opsForZSet().incrementScore(key, keyword, 1.0);
    }

    private void decrementChallengeKeywords(Long userId, String keyword) {
        String key = getRedisKey(USER_CHALLENGE_KEYWORDS, userId);
        Double score = redisTemplate.opsForZSet().incrementScore(key, keyword, -1.0);

        if (score != null && score <= 0) {
            redisTemplate.opsForZSet().remove(key, keyword);
        }
    }

    private List<String> processKeywords(String title) {
        StringTokenizer tokenizer = new StringTokenizer(title);
        List<String> keywords = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().toLowerCase();
            if (!stopwords.contains(token)) {
                keywords.add(token);
            }
        }

        return keywords;
    }

    private String getRedisKey(String key, Long userId) {
        return String.format(key, userId);
    }
}
