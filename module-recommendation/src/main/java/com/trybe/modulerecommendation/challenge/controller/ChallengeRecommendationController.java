package com.trybe.modulerecommendation.challenge.controller;

import com.trybe.moduleapi.challenge.dto.ChallengeRecommendationRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.modulerecommendation.challenge.service.ChallengeRecommendationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendation/challenges")
public class ChallengeRecommendationController {
    private final ChallengeRecommendationService challengeRecommendationService;

    public ChallengeRecommendationController(ChallengeRecommendationService challengeRecommendationService) {
        this.challengeRecommendationService = challengeRecommendationService;
    }

    @PostMapping
    public List<ChallengeResponse.Preview> getChallengeRecommendations(@RequestBody ChallengeRecommendationRequest request) {
        return challengeRecommendationService.getChallengeRecommendations(request);
    }
}