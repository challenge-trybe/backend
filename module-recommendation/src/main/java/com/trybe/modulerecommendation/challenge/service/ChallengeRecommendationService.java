package com.trybe.modulerecommendation.challenge.service;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChallengeRecommendationService {
    private final ChallengeRepository challengeRepository;
    private final ChallengePreferenceCache challengePreferenceCache;

    public ChallengeRecommendationService(ChallengeRepository challengeRepository, ChallengePreferenceCache challengePreferenceCache) {
        this.challengeRepository = challengeRepository;
        this.challengePreferenceCache = challengePreferenceCache;
    }

    private static final int LIMIT = 20;
    private static final int RECOMMENDATION_CATEGORY_COUNT = 3;
    private static final int RECOMMENDATION_KEYWORD_COUNT = 10;

    public List<Challenge> getChallengeRecommendations(Long userId) {
        List<ChallengeCategory> categories = challengePreferenceCache.getPreferenceCategories(userId, RECOMMENDATION_CATEGORY_COUNT);
        List<String> keywords = challengePreferenceCache.getPreferenceKeywords(userId, RECOMMENDATION_KEYWORD_COUNT);

        List<Challenge> recommendations = challengeRepository.getRecommendedChallenges(categories, keywords, LIMIT);

        Set<Challenge> challengeSet = new LinkedHashSet<>(recommendations);

        return new ArrayList<>(challengeSet);
    }
}