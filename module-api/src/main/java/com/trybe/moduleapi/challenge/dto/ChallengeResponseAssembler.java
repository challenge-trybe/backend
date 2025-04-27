package com.trybe.moduleapi.challenge.dto;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import org.springframework.stereotype.Component;

@Component
public class ChallengeResponseAssembler {
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;

    public ChallengeResponseAssembler(ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
    }

    public ChallengeResponse.Detail toDetail(Challenge challenge, Long userId) {
        Long challengeId = challenge.getId();

        int participantCount = getParticipantCount(challengeId);
        ChallengeResponse.Bookmark bookmark = toBookmark(challengeId, userId);

        return ChallengeResponse.Detail.from(challenge, participantCount, bookmark);
    }

    public ChallengeResponse.Preview toPreview(Challenge challenge, Long userId) {
        Long challengeId = challenge.getId();

        int participantCount = getParticipantCount(challengeId);
        ChallengeResponse.Bookmark bookmark = toBookmark(challengeId, userId);

        return ChallengeResponse.Preview.from(challenge, participantCount, bookmark);
    }

    public ChallengeResponse.Summary toSummary(Challenge challenge) {
        return ChallengeResponse.Summary.from(challenge);
    }

    private int getParticipantCount(Long challengeId) {
        return challengeParticipationRepository.countByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED);
    }

    private ChallengeResponse.Bookmark toBookmark(Long challengeId, Long userId) {
        int bookmarkCount = challengeBookmarkCache.getBookmarkCount(challengeId);
        Boolean bookmarked = userId == null ? null : challengeBookmarkCache.isBookmarked(userId, challengeId);
        return new ChallengeResponse.Bookmark(bookmarkCount, bookmarked);
    }
}
