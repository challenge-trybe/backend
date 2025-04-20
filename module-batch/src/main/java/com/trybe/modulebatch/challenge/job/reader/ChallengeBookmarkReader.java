package com.trybe.modulebatch.challenge.job.reader;

import com.trybe.modulebatch.challenge.job.dto.ChallengeBookmarkDto;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRedisCache;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.Cursor;

import java.util.Iterator;
import java.util.Set;

public abstract class ChallengeBookmarkReader implements ItemReader<ChallengeBookmarkDto> {
    protected final ChallengeBookmarkRedisCache challengeBookmarkCache;

    public ChallengeBookmarkReader(ChallengeBookmarkRedisCache challengeBookmarkCache) {
        this.challengeBookmarkCache = challengeBookmarkCache;
    }

    private Cursor<String> cursor;
    private Iterator<Long> currentUserIdsIterator;
    private Long currentChallengeId;

    private static final int REDIS_SCAN_COUNT = 100;

    protected abstract String getPattern();
    protected abstract Long getChallengeId(String key);
    protected abstract Set<Long> getUserIds(Long challengeId);

    @Override
    public ChallengeBookmarkDto read() throws Exception {
        if (cursor == null) {
            initializeCursor();
        }

        if (currentUserIdsIterator == null || !currentUserIdsIterator.hasNext()) {
            if (!loadNextChallenge()) {
                return null;
            }
        }

        Long userId = currentUserIdsIterator.next();
        return new ChallengeBookmarkDto(userId, currentChallengeId);
    }

    private void initializeCursor() {
        cursor = challengeBookmarkCache.getKeysByPattern(getPattern(), REDIS_SCAN_COUNT);
    }

    private boolean loadNextChallenge() {
        while (cursor.hasNext()) {
            String key = cursor.next();
            currentChallengeId = getChallengeId(key);

            Set<Long> userIds = getUserIds(currentChallengeId);
            if (userIds != null && !userIds.isEmpty()) {
                currentUserIdsIterator = userIds.iterator();
                return true;
            }
        }
        return false;
    }
}
