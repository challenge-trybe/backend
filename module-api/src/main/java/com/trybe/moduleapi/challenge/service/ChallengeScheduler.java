package com.trybe.moduleapi.challenge.service;

import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChallengeScheduler {
    private final ChallengeRepository challengeRepository;

    public ChallengeScheduler(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    @Scheduled(cron = "0 4 * * * *")
    @Transactional
    public void updateChallengeStatusOnGoing() {
        List<Challenge> challenges = challengeRepository.findAllByStatusAndStartDate(ChallengeStatus.PENDING, LocalDate.now());
        challenges.forEach(challenge -> {
            challenge.updateStatus(ChallengeStatus.ONGOING);
        });
    }

    @Scheduled(cron = "59 59 23 * * *")
    @Transactional
    public void updateChalengeStatusDone() {
        List<Challenge> challenges = challengeRepository.findAllByStatusAndEndDate(ChallengeStatus.ONGOING, LocalDate.now());
        challenges.forEach(challenge -> {
            challenge.updateStatus(ChallengeStatus.DONE);
        });
    }
}
