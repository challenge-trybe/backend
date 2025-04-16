package com.trybe.modulebatch.challenge.job.reader;

import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRedisCache;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ChallengeBookmarkInsertReader extends ChallengeBookmarkReader {
    public ChallengeBookmarkInsertReader(ChallengeBookmarkRedisCache challengeBookmarkCache) {
        super(challengeBookmarkCache);
    }

    @Override
    protected String getPattern() {
        return ChallengeBookmarkRedisCache.CHALLENGE_BOOKMARK_ADDED_PATTERN;
    }

    @Override
    protected Long getChallengeId(String key) {
        String[] tokens = key.split(":");
        return Long.parseLong(tokens[1]);
    }

    @Override
    protected Set<Long> getUserIds(Long challengeId) {
        return challengeBookmarkCache.getBookmarkAddedUsers(challengeId);
    }
}
