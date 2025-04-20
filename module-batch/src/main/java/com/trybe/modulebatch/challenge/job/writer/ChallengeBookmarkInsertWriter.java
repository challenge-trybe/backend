package com.trybe.modulebatch.challenge.job.writer;

import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChallengeBookmarkInsertWriter extends ChallengeBookmarkWriter {
    public ChallengeBookmarkInsertWriter(ChallengeBookmarkRepository challengeBookmarkRepository) {
        super(challengeBookmarkRepository);
    }

    @Override
    protected void writeBookmarks(List<ChallengeBookmark> bookmarks) {
        challengeBookmarkRepository.bulkInsert(bookmarks);
    }
}
