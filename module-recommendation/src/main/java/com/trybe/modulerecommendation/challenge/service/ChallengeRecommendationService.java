package com.trybe.modulerecommendation.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRecommendationRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeRecommendationService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;

    public ChallengeRecommendationService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
    }

    private static final int MIN_LIMIT = 20;
    private static final int MAX_LIMIT = 40;

    public List<ChallengeResponse.Preview> getChallengeRecommendations(ChallengeRecommendationRequest request) {
        List<Challenge> challenges = challengeRepository.getByCategoriesOrKeywords(request.categories(), request.keywords(), MAX_LIMIT);

        List<ChallengeResponse.Preview> challengePreviews = challenges.stream()
                .map(challenge -> createPreview(request.userId(), challenge))
                .toList();

        return challengePreviews;
    }

    private ChallengeResponse.Preview createPreview(Long userId, Challenge challenge) {
        int participation = getParticipationCount(challenge.getId());
        ChallengeResponse.Bookmark bookmark = createBookmark(userId, challenge.getId());
        return ChallengeResponse.Preview.from(challenge, participation, bookmark);
    }

    private ChallengeResponse.Bookmark createBookmark(Long userId, Long challengeId) {
        int bookmarkCount = challengeBookmarkCache.getBookmarkCount(challengeId);
        Boolean bookmarked = userId == null ? null : challengeBookmarkCache.isBookmarked(userId, challengeId);
        return new ChallengeResponse.Bookmark(bookmarkCount, bookmarked);
    }

    private int getParticipationCount(Long challengeId) {
        return challengeParticipationRepository.countByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED);
    }
}