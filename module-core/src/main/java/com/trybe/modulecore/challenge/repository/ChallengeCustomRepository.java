package com.trybe.modulecore.challenge.repository;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeCategory;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChallengeCustomRepository {
    Page<Challenge> getFilteredChallenges(String keyword, List<ChallengeCategory> categories, List<ChallengeStatus> statuses, Pageable pageable);
    List<Challenge> getRecommendedChallenges(List<ChallengeCategory> categories, List<String> keywords, int limit);
}
