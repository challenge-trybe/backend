package com.trybe.modulecore.challenge.repository;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;

import java.util.List;

public interface ChallengeCustomRepository {
    List<Challenge> getByCategoriesOrKeywords(List<ChallengeCategory> categories, List<String> keywords, int limit);
}
