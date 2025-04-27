package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.client.ChallengeRecommendationClient;
import com.trybe.modulecore.challenge.entity.Challenge;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeRecommendationClientService {
    private final ChallengeRecommendationClient challengeRecommendationClient;
    private final PopularChallengeService popularChallengeService;

    public ChallengeRecommendationClientService(ChallengeRecommendationClient challengeRecommendationClient, PopularChallengeService popularChallengeService) {
        this.challengeRecommendationClient = challengeRecommendationClient;
        this.popularChallengeService = popularChallengeService;
    }

    private static final int MIN_LIMIT = 20;

    @CircuitBreaker(name = "challengeRecommendation", fallbackMethod = "fallbackGetChallengeRecommendations")
    public List<Challenge> getChallengeRecommendations(Long userId) {
        return challengeRecommendationClient.getChallengeRecommendations(userId);
    }

    public List<Challenge> fallbackGetChallengeRecommendations(Long userId, Throwable throwable) {
        return popularChallengeService.getTopPopularChallenges(MIN_LIMIT);
    }
}