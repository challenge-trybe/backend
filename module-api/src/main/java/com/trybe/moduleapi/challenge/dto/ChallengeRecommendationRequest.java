package com.trybe.moduleapi.challenge.dto;

import com.trybe.modulecore.challenge.enums.ChallengeCategory;

import java.util.List;

public record ChallengeRecommendationRequest(
        Long userId,
        List<ChallengeCategory> categories,
        List<String> keywords
) {}