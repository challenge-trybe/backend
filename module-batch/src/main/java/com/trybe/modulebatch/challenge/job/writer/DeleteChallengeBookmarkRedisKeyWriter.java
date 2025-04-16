package com.trybe.modulebatch.challenge.job.writer;

import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRedisCache;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeleteChallengeBookmarkRedisKeyWriter implements ItemWriter<String> {
    private final ChallengeBookmarkRedisCache challengeBookmarkRedisCache;

    public DeleteChallengeBookmarkRedisKeyWriter(ChallengeBookmarkRedisCache challengeBookmarkRedisCache) {
        this.challengeBookmarkRedisCache = challengeBookmarkRedisCache;
    }

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        List<String> items = chunk.getItems().stream()
                .map(item -> (String) item)
                .toList();

        if (items.isEmpty()) return;

        challengeBookmarkRedisCache.deleteKeys(items);
    }
}
