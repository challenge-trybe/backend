package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.client.ChallengeRecommendationClient;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class ChallengeRecommendationClientService {
    private final ChallengeRecommendationClient challengeRecommendationClient;
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;
    private final PopularChallengeService popularChallengeService;

    public ChallengeRecommendationClientService(ChallengeRecommendationClient challengeRecommendationClient, ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache, PopularChallengeService popularChallengeService) {
        this.challengeRecommendationClient = challengeRecommendationClient;
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
        this.popularChallengeService = popularChallengeService;
    }

    private static final int MIN_LIMIT = 20;

    @CircuitBreaker(name = "challengeRecommendation", fallbackMethod = "fallbackGetChallengeRecommendations")
    public List<ChallengeResponse.Preview> getChallengeRecommendations(Long userId) {
        return challengeRecommendationClient.getChallengeRecommendations(userId);
    }

    public List<ChallengeResponse.Preview> fallbackGetChallengeRecommendations(Long userId, Throwable throwable) {
        List<Challenge> challenges = popularChallengeService.getTopPopularChallenges(MIN_LIMIT);

        Collections.shuffle(challenges);
        return challenges.stream()
                .map(challenge -> createPreview(userId, challenge))
                .toList();
    }

    private ChallengeResponse.Preview createPreview(Long userId, Challenge challenge) {
        int participationCount = getParticipationCount(challenge.getId());
        ChallengeResponse.Bookmark bookmark = createBookmark(userId, challenge.getId());
        return ChallengeResponse.Preview.from(challenge, participationCount, bookmark);
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