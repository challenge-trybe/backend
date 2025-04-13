package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.utils.DateUtils;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.popular.PopularChallengeCache;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PopularChallengeService {
    private final PopularChallengeCache popularChallengeCache;
    private final ChallengeRepository challengeRepository;

    public PopularChallengeService(PopularChallengeCache popularChallengeCache, ChallengeRepository challengeRepository) {
        this.popularChallengeCache = popularChallengeCache;
        this.challengeRepository = challengeRepository;
    }

    public void increasePopularity(Long challengeId, int score) {
        popularChallengeCache.increaseScore(challengeId, score, DateUtils.getToday());
    }

    public void decreasePopularity(Long challengeId, int score) {
        popularChallengeCache.decreaseScore(challengeId, score, DateUtils.getToday());
    }

    public List<Challenge> getTopPopularChallenges(int count) {
        Set<Long> challengeIds = popularChallengeCache.getTopPopularChallenges(DateUtils.getToday().minusDays(1), count);
        List<Challenge> challenges = challengeIds.isEmpty()
                ? Collections.emptyList()
                : challengeRepository.findAllByIdIn(challengeIds);

        return sortChallenges(challenges, challengeIds);
    }

    private List<Challenge> sortChallenges(List<Challenge> challenges, Set<Long> challengeIds) {
        Map<Long, Challenge> challengeMap = challenges.stream()
                .collect(Collectors.toMap(Challenge::getId, challenge -> challenge));

        return challengeIds.stream()
                .map(challengeMap::get)
                .collect(Collectors.toList());
    }
}
