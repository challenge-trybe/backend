package com.trybe.modulebatch.challenge.job.reader;

import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRedisCache;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.stereotype.Component;

@Component
public class DeleteChallengeBookmarkRedisKeyReader implements ItemReader<String> {
    private final ChallengeBookmarkRedisCache challengeBookmarkRedisCache;

    public DeleteChallengeBookmarkRedisKeyReader(ChallengeBookmarkRedisCache challengeBookmarkRedisCache) {
        this.challengeBookmarkRedisCache = challengeBookmarkRedisCache;
    }

    private Cursor<String> addedCursor;
    private Cursor<String> deletedCursor;

    private static final int REDIS_SCAN_COUNT = 100;

    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (addedCursor == null && deletedCursor == null) {
            initializeCursors();
        }

        if (addedCursor != null && addedCursor.hasNext()) {
            return addedCursor.next();
        } else if (deletedCursor != null && deletedCursor.hasNext()) {
            return deletedCursor.next();
        } else {
            return null;
        }
    }

    private void initializeCursors() {
        addedCursor = challengeBookmarkRedisCache.getKeysByPattern(ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_ADDED_PATTERN, REDIS_SCAN_COUNT);
        deletedCursor = challengeBookmarkRedisCache.getKeysByPattern(ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_DELETED_PATTERN, REDIS_SCAN_COUNT);
    }
}
