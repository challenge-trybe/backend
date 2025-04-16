package com.trybe.modulecore.challenge.repository.bookmark;

import com.trybe.modulecore.challenge.entity.ChallengeBookmark;

import java.util.List;

public interface ChallengeBookmarkCustomRepository {
    void bulkInsert(List<ChallengeBookmark> bookmarks);
    void bulkDelete(List<ChallengeBookmark> bookmarks);
}
