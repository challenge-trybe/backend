package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChallengeBookmarkService {
    private final ChallengeRepository challengeRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    public ChallengeBookmarkService(ChallengeRepository challengeRepository, RedisTemplate<String, Long> redisTemplate) {
        this.challengeRepository = challengeRepository;
        this.redisTemplate = redisTemplate;
    }

    private final String BOOKMARK_SUFFIX = ":bookmark";
    private final String USER_KEY = "user:%d" + BOOKMARK_SUFFIX;
    private final String CHALLENGE_BOOKMARK_KEY = "challenge:%d" + BOOKMARK_SUFFIX;
    private final String CHALLENGE_BOOKMARK_COUNT_KEY = CHALLENGE_BOOKMARK_KEY + ":count";
    private final String CHALLENGE_BOOKMARK_DELETED_KEY = CHALLENGE_BOOKMARK_KEY + ":deleted";

    @Transactional
    public ChallengeResponse.Bookmark addBookmark(User user, Long challengeId) {
        validateExistChallenge(challengeId);

        String userKey = getRedisKey(USER_KEY, user.getId());
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        String challengeCountKey = getRedisKey(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId);
        String challengeDeletedKey = getRedisKey(CHALLENGE_BOOKMARK_DELETED_KEY, challengeId);
        int count = getChallengeBookmarkCount(challengeId);

        if (!redisTemplate.opsForSet().isMember(challengeKey, user.getId())) {
            double score = getCurrentTimeInSeconds();
            redisTemplate.opsForZSet().add(userKey, challengeId, score);
            redisTemplate.opsForSet().add(challengeKey, user.getId());
            redisTemplate.opsForSet().remove(challengeDeletedKey, user.getId());
            redisTemplate.opsForValue().increment(challengeCountKey, 1L);
            count++;
        }

        return new ChallengeResponse.Bookmark(count, true);
    }

    @Transactional
    public ChallengeResponse.Bookmark removeBookmark(User user, Long challengeId) {
        validateExistChallenge(challengeId);

        String userKey = getRedisKey(USER_KEY, user.getId());
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        String challengeCountKey = getRedisKey(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId);
        String challengeDeletedKey = getRedisKey(CHALLENGE_BOOKMARK_DELETED_KEY, challengeId);
        int count = getChallengeBookmarkCount(challengeId);

        if (redisTemplate.opsForSet().isMember(challengeKey, user.getId())) {
            redisTemplate.opsForZSet().remove(userKey, challengeId);
            redisTemplate.opsForSet().remove(challengeKey, user.getId());
            redisTemplate.opsForSet().add(challengeDeletedKey, user.getId());
            redisTemplate.opsForValue().decrement(challengeCountKey, 1L);
            count--;
        }

        return new ChallengeResponse.Bookmark(count, false);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeResponse.Summary> getMyBookmarkedChallenges(User user, Pageable pageable) {
        String userKey = getRedisKey(USER_KEY, user.getId());

        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;

        Set<Long> challengeIds = Optional.ofNullable(redisTemplate.opsForZSet().reverseRange(userKey, start, end)).orElse(Collections.emptySet());

        List<Challenge> challenges = challengeIds.isEmpty()
                ? Collections.emptyList()
                : challengeRepository.findAllByIdIn(challengeIds);

        List<ChallengeResponse.Summary> challengeSummaries = sortChallenges(challenges, challengeIds).stream()
                .map(challenge -> ChallengeResponse.Summary.from(challenge, getChallengeBookmarkCount(challenge.getId()), true))
                .collect(Collectors.toList());

        Page<ChallengeResponse.Summary> challengePage = new PageImpl<>(challengeSummaries, pageable, getUserBookmarkCount(user.getId()));

        return new PageResponse<>(challengePage);
    }

    public boolean isBookmarked(Long userId, Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        return redisTemplate.opsForSet().isMember(challengeKey, userId);
    }

    public int getChallengeBookmarkCount(Long challengeId) {
        String challengeCountKey = getRedisKey(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId);
        Long count = redisTemplate.opsForValue().get(challengeCountKey);

        return count == null ? 0 : count.intValue();
    }

    public void removeBookmarksByChallenge(Long challengeId) {
        String challengeKey = getRedisKey(CHALLENGE_BOOKMARK_KEY, challengeId);
        String challengeCountKey = getRedisKey(CHALLENGE_BOOKMARK_COUNT_KEY, challengeId);
        String challengeDeletedKey = getRedisKey(CHALLENGE_BOOKMARK_DELETED_KEY, challengeId);

        Set<Long> userIds = Optional.ofNullable(redisTemplate.opsForSet().members(challengeKey)).orElse(Collections.emptySet());

        userIds.forEach(userId -> {
            String userKey = getRedisKey(USER_KEY, userId);
            redisTemplate.opsForZSet().remove(userKey, challengeId);
        });

        redisTemplate.delete(List.of(challengeKey, challengeCountKey, challengeDeletedKey));
    }

    private List<Challenge> sortChallenges(List<Challenge> challenges, Set<Long> challengeIds) {
        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, challenge -> challenge));

        return challengeIds.stream()
                .map(challengeMap::get)
                .collect(Collectors.toList());
    }

    private void validateExistChallenge(Long challengeId) {
        if (!challengeRepository.existsById(challengeId)) {
            throw new NotFoundChallengeException();
        }
    }

    private int getUserBookmarkCount(Long userId) {
        String userKey = getRedisKey(USER_KEY, userId);
        Long count = redisTemplate.opsForZSet().size(userKey);

        return count == null ? 0 : count.intValue();
    }

    private String getRedisKey(String key, Long id) {
        return String.format(key, id);
    }

    private double getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}