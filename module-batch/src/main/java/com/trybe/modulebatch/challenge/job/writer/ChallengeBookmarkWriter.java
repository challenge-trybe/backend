package com.trybe.modulebatch.challenge.job.writer;

import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public abstract class ChallengeBookmarkWriter implements ItemWriter<ChallengeBookmark> {
    protected final ChallengeBookmarkRepository challengeBookmarkRepository;

    public ChallengeBookmarkWriter(ChallengeBookmarkRepository challengeBookmarkRepository) {
        this.challengeBookmarkRepository = challengeBookmarkRepository;
    }

    protected abstract void writeBookmarks(List<ChallengeBookmark> bookmarks);

    @Override
    public void write(Chunk<? extends ChallengeBookmark> chunk) throws Exception {
        List<ChallengeBookmark> items = chunk.getItems().stream()
                .map(item -> (ChallengeBookmark) item)
                .toList();

        if (items.isEmpty()) return;

        writeBookmarks(items);
    }
}
