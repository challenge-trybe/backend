package com.trybe.modulerecommendation.challenge.controller;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulerecommendation.challenge.service.ChallengeRecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendation/challenges")
public class ChallengeRecommendationController {
    private final ChallengeRecommendationService challengeRecommendationService;

    public ChallengeRecommendationController(ChallengeRecommendationService challengeRecommendationService) {
        this.challengeRecommendationService = challengeRecommendationService;
    }

    @GetMapping
    public List<Challenge> getChallengeRecommendations(@RequestParam Long userId) {
        return challengeRecommendationService.getChallengeRecommendations(userId);
    }
}