package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.client.ChallengeRecommendationClient;
import com.trybe.moduleapi.challenge.dto.ChallengeRecommendationRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChallengeRecommendationClientService {
    private final ChallengeRecommendationClient challengeRecommendationClient;

    public ChallengeRecommendationClientService(ChallengeRecommendationClient challengeRecommendationClient) {
        this.challengeRecommendationClient = challengeRecommendationClient;
    }

    @CircuitBreaker(name = "challengeRecommendation", fallbackMethod = "fallbackGetChallengeRecommendations")
    public List<ChallengeResponse.Preview> getChallengeRecommendations(ChallengeRecommendationRequest request) {
        return challengeRecommendationClient.getChallengeRecommendations(request);
    }

    public List<ChallengeResponse.Preview> fallbackGetChallengeRecommendations(ChallengeRecommendationRequest request, Throwable throwable) {
        return List.of();
    }
}
