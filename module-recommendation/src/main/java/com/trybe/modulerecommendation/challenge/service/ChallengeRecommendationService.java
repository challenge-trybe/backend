package com.trybe.modulerecommendation.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.bookmark.ChallengeBookmarkCache;
import com.trybe.modulecore.challenge.repository.popular.PopularChallengeCache;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChallengeRecommendationService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeBookmarkCache challengeBookmarkCache;
    private final ChallengePreferenceCache challengePreferenceCache;
    private final PopularChallengeCache popularChallengeCache;

    public ChallengeRecommendationService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChallengeBookmarkCache challengeBookmarkCache, ChallengePreferenceCache challengePreferenceCache, PopularChallengeCache popularChallengeCache) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeBookmarkCache = challengeBookmarkCache;
        this.challengePreferenceCache = challengePreferenceCache;
        this.popularChallengeCache = popularChallengeCache;
    }

    private static final int LIMIT = 20;
    private static final int RECOMMENDATION_CATEGORY_COUNT = 3;
    private static final int RECOMMENDATION_KEYWORD_COUNT = 10;
    public static final int INITIALIZE_HOUR = 4;

    public List<ChallengeResponse.Preview> getChallengeRecommendations(Long userId) {
        List<ChallengeCategory> categories = challengePreferenceCache.getPreferenceCategories(userId, RECOMMENDATION_CATEGORY_COUNT);
        List<String> keywords = challengePreferenceCache.getPreferenceKeywords(userId, RECOMMENDATION_KEYWORD_COUNT);

        List<Challenge> recommendations = challengeRepository.getByCategoriesOrKeywords(categories, keywords, LIMIT);

        LinkedHashSet<Challenge> challengeSet = new LinkedHashSet<>(recommendations);

        if (challengeSet.size() < LIMIT) {
            challengeSet.addAll(getPopularChallenges(LIMIT));
        }

        List<Challenge> challenges = new ArrayList<>(challengeSet);
        if (challenges.size() > LIMIT) {
            challenges = challenges.subList(0, LIMIT);
        }

        Collections.shuffle(challenges);

        List<ChallengeResponse.Preview> challengePreviews = challenges.stream()
                .map(challenge -> createPreview(userId, challenge))
                .toList();

        return challengePreviews;
    }

    private List<Challenge> getPopularChallenges(int count) {
        Set<Long> challengeIds = popularChallengeCache.getTopPopularChallenges(getToday(), count);
        return challengeRepository.findAllByIdIn(challengeIds);
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

    private LocalDate getToday() {
        LocalDateTime now = LocalDateTime.now();

        return (now.getHour() < INITIALIZE_HOUR) ? now.toLocalDate().minusDays(1) : now.toLocalDate();
    }
}