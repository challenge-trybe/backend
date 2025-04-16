package com.trybe.modulecore.challenge.repository.bookmark;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class ChallengeBookmarkRedisCache implements ChallengeBookmarkCache {
    private final RedisTemplate<String, Long> redisTemplate;

    public ChallengeBookmarkRedisCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final String BOOKMARK_SUFFIX = ":bookmarks";
    private final String USER_SUFFIX = ":users";
    private final String CHALLENGE_SUFFIX = ":challenges";

    private final String USER_KEY = "user:%d" + BOOKMARK_SUFFIX + CHALLENGE_SUFFIX;
    private final String CHALLENGE_BOOKMARK_KEY = "challenge:%d" + BOOKMARK_SUFFIX + USER_SUFFIX;
    private final String CHALLENGE_BOOKMARK_COUNT_KEY = "challenge" + BOOKMARK_SUFFIX + ":counts";
    private final String CHALLENGE_BOOKMARK_ADDED_KEY = "challenge:%d" + BOOKMARK_SUFFIX + ":added" + USER_SUFFIX;
    private final String CHALLENGE_BOOKMARK_DELETED_KEY = "challenge:%d" + BOOKMARK_SUFFIX + ":deleted" + USER_SUFFIX;

    @Override
    public void addBookmark(Long userId, Long challengeId) {
        addUserChallengeBookmark(userId, challengeId);
        addChallengeUserBookmark(userId, challengeId);
        addChallengeBookmarkAdded(userId, challengeId);
        removeChallengeBookmarkDeleted(userId, challengeId);
        incrementChallengeBookmarkScore(challengeId);
    }

    @Override
    public void removeBookmark(Long userId, Long challengeId) {
        removeUserChallengeBookmark(userId, challengeId);
        removeChallengeUserBookmark(userId, challengeId);
        removeChallengeBookmarkAdded(userId, challengeId);
        addChallengeBookmarkDeleted(userId, challengeId);
        decrementChallengeBookmarkScore(challengeId);
    }

    @Override
    public boolean isBookmarked(Long userId, Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(challengeKey, userId));
    }

    @Override
    public int getBookmarkCount(Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        Long count = redisTemplate.opsForSet().size(challengeKey);

        return count == null ? 0 : count.intValue();
    }

    @Override
    public int getUserBookmarkCount(Long userId) {
        String userKey = getRedisKey(USER_KEY, userId);
        Long count = redisTemplate.opsForZSet().zCard(userKey);

        return count == null ? 0 : count.intValue();
    }

    @Override
    public Set<Long> getBookmarkedChallenges(Long userId, int start, int end) {
        String userKey = getRedisKey(USER_KEY, userId);

        return Optional.ofNullable(redisTemplate.opsForZSet().reverseRange(userKey, start, end))
                .orElse(Collections.emptySet());
    }

    @Override
    public Set<Long> getMostBookmarkedChallenges(int count) {
        return Optional.ofNullable(redisTemplate.opsForZSet().reverseRange(CHALLENGE_BOOKMARK_COUNT_KEY, 0, count - 1))
                .orElse(Collections.emptySet());
    }

    @Override
    public void removeBookmarksByChallenge(Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        String challengeAddedKey = getRedisKey(CHALLENGE_BOOKMARK_ADDED_KEY, challengeId);

        Set<Long> userIds = getBookmarkedUsers(challengeId);

        userIds.forEach(userId -> {
            removeUserChallengeBookmark(userId, challengeId);
            addChallengeBookmarkDeleted(userId, challengeId);
        });

        redisTemplate.opsForZSet().remove(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId);
        redisTemplate.delete(List.of(challengeKey, challengeAddedKey));
    }

    private void addUserChallengeBookmark(Long userId, Long challengeId) {
        String userKey = getRedisKey(USER_KEY, userId);
        double score = getCurrentTimeInSeconds();
        redisTemplate.opsForZSet().add(userKey, challengeId, score);
    }

    private void removeUserChallengeBookmark(Long userId, Long challengeId) {
        String userKey = getRedisKey(USER_KEY, userId);
        redisTemplate.opsForZSet().remove(userKey, challengeId);
    }

    private void addChallengeUserBookmark(Long userId, Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        redisTemplate.opsForSet().add(challengeKey, userId);
    }

    private void removeChallengeUserBookmark(Long userId, Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        redisTemplate.opsForSet().remove(challengeKey, userId);
    }

    private void addChallengeBookmarkAdded(Long userId, Long challengeId) {
        String challengeAddedKey = getRedisKey(CHALLENGE_BOOKMARK_ADDED_KEY, challengeId);
        redisTemplate.opsForSet().add(challengeAddedKey, userId);
    }

    private void removeChallengeBookmarkAdded(Long userId, Long challengeId) {
        String challengeAddedKey = getRedisKey(CHALLENGE_BOOKMARK_ADDED_KEY, challengeId);
        redisTemplate.opsForSet().remove(challengeAddedKey, userId);
    }

    private void addChallengeBookmarkDeleted(Long userId, Long challengeId) {
        String challengeDeletedKey = getRedisKey(CHALLENGE_BOOKMARK_DELETED_KEY, challengeId);
        redisTemplate.opsForSet().add(challengeDeletedKey, userId);
    }

    private void removeChallengeBookmarkDeleted(Long userId, Long challengeId) {
        String challengeDeletedKey = getRedisKey(CHALLENGE_BOOKMARK_DELETED_KEY, challengeId);
        redisTemplate.opsForSet().remove(challengeDeletedKey, userId);
    }

    private void incrementChallengeBookmarkScore(Long challengeId) {
        redisTemplate.opsForZSet().incrementScore(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId, 1.0);
    }

    private void decrementChallengeBookmarkScore(Long challengeId) {
        Double score = redisTemplate.opsForZSet().incrementScore(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId, -1.0);

        if (score != null && score <= 0) {
            redisTemplate.opsForZSet().remove(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId);
        }
    }

    private Set<Long> getBookmarkedUsers(Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);

        return Optional.ofNullable(redisTemplate.opsForSet().members(challengeKey))
                .orElse(Collections.emptySet());
    }

    private String getRedisKey(String key, Long id) {
        return String.format(key, id);
    }

    private double getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
