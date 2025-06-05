package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.event.model.ChallengeStatusEvent;
import com.trybe.moduleapi.challenge.event.pub.ChallengeEventPublisher;
import com.trybe.moduleapi.challenge.event.type.ChallengeStatusEventType;
import com.trybe.moduleapi.chat.service.ChatService;
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
    private final ChatService chatService;
    private final ChallengeEventPublisher challengeEventPublisher;

    public ChallengeScheduler(ChallengeRepository challengeRepository, ChatService chatService, ChallengeEventPublisher challengeEventPublisher) {
        this.challengeRepository = challengeRepository;
        this.chatService = chatService;
        this.challengeEventPublisher = challengeEventPublisher;
    }

    @Scheduled(cron = " 0 0 4 * * *")
    @Transactional
    public void updateChallengeStatusOnGoing() {
        List<Challenge> challenges = challengeRepository.findAllByStatusAndStartDate(ChallengeStatus.PENDING, LocalDate.now());

        challenges.forEach(challenge -> {
            challenge.updateStatus(ChallengeStatus.ONGOING);
            chatService.challengeStartMessage(challenge);
            challengeEventPublisher.publish(new ChallengeStatusEvent(challenge, ChallengeStatusEventType.START));
        });
    }

    @Scheduled(cron = "59 59 23 * * *")
    @Transactional
    public void updateChallengeStatusDone() {
        List<Challenge> challenges = challengeRepository.findAllByStatusAndEndDate(ChallengeStatus.ONGOING, LocalDate.now());
        challenges.forEach(challenge -> {
            challenge.updateStatus(ChallengeStatus.DONE);
            chatService.challengeClosedMessage(challenge);
            challengeEventPublisher.publish(new ChallengeStatusEvent(challenge, ChallengeStatusEventType.END));
        });
    }
}
