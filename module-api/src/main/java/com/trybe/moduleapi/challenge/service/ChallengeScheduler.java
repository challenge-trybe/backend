package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.event.model.ChallengeStatusEvent;
import com.trybe.moduleapi.challenge.event.pub.ChallengeEventPublisher;
import com.trybe.moduleapi.challenge.event.type.ChallengeStatusEventType;
import com.trybe.moduleapi.chat.service.ChatService;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ChallengeScheduler {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChatService chatService;
    private final ChallengeEventPublisher challengeEventPublisher;

    public ChallengeScheduler(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository, ChatService chatService, ChallengeEventPublisher challengeEventPublisher) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.chatService = chatService;
        this.challengeEventPublisher = challengeEventPublisher;
    }

    @Scheduled(cron = " 0 0 4 * * *")
    @Transactional
    public void updateChallengeStatusOnGoing() {
        List<Challenge> challenges = challengeRepository.findAllByStatusAndStartDate(ChallengeStatus.PENDING, LocalDate.now());

        challenges.forEach(challenge -> {
            List<User> participants = getParticipants(challenge.getId());

            challenge.updateStatus(ChallengeStatus.ONGOING);
            chatService.challengeStartMessage(challenge);
            challengeEventPublisher.publish(new ChallengeStatusEvent(challenge, ChallengeStatusEventType.START, participants));
        });
    }

    @Scheduled(cron = "59 59 23 * * *")
    @Transactional
    public void updateChallengeStatusDone() {
        List<Challenge> challenges = challengeRepository.findAllByStatusAndEndDate(ChallengeStatus.ONGOING, LocalDate.now());
        challenges.forEach(challenge -> {
            List<User> participants = getParticipants(challenge.getId());

            challenge.updateStatus(ChallengeStatus.DONE);
            chatService.challengeClosedMessage(challenge);
            challengeEventPublisher.publish(new ChallengeStatusEvent(challenge, ChallengeStatusEventType.END, participants));
        });
    }

    private List<User> getParticipants(Long challengeId) {
        return challengeParticipationRepository.findAllByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED)
                .stream()
                .map(ChallengeParticipation::getUser)
                .toList();
    }
}
