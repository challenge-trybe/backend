package com.trybe.modulebatch.challenge.job.writer;

import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChallengeBookmarkDeleteWriter extends ChallengeBookmarkWriter {
    public ChallengeBookmarkDeleteWriter(ChallengeBookmarkRepository challengeBookmarkRepository) {
        super(challengeBookmarkRepository);
    }

    @Override
    protected void writeBookmarks(List<ChallengeBookmark> bookmarks) {
        challengeBookmarkRepository.bulkDelete(bookmarks);
    }
}
