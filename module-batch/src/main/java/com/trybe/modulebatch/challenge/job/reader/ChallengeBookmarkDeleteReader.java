package com.trybe.modulebatch.challenge.job.reader;

import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRedisCache;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ChallengeBookmarkDeleteReader extends ChallengeBookmarkReader {
    public ChallengeBookmarkDeleteReader(ChallengeBookmarkRedisCache challengeBookmarkCache) {
        super(challengeBookmarkCache);
    }

    @Override
    protected String getPattern() {
        return ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_DELETED_PATTERN;
    }

    @Override
    protected Long getChallengeId(String key) {
        String[] tokens = key.split(":");
        return Long.parseLong(tokens[1]);
    }

    @Override
    protected Set<Long> getUserIds(Long challengeId) {
        return challengeBookmarkCache.getBookmarkDeletedUsers(challengeId);
    }
}
