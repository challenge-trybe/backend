package com.trybe.moduleapi.challenge.event.listener;

import com.trybe.moduleapi.challenge.event.model.ChallengeParticipationEvent;
import com.trybe.moduleapi.challenge.event.model.ChallengeStatusEvent;
import com.trybe.moduleapi.challenge.event.type.ChallengeParticipationEventType;
import com.trybe.moduleapi.challenge.event.type.ChallengeStatusEventType;
import com.trybe.moduleapi.challenge.exception.participation.NotFoundChallengeParticipationException;
import com.trybe.moduleapi.notification.constants.NotificationTopics;
import com.trybe.moduleapi.notification.service.NotificationProducerService;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.notification.enums.NotificationType;
import com.trybe.modulecore.user.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class ChallengeNotificationEventListener {
    private final NotificationProducerService notificationProducerService;
    private final ChallengeParticipationRepository challengeParticipationRepository;

    public ChallengeNotificationEventListener(NotificationProducerService notificationProducerService, ChallengeParticipationRepository challengeParticipationRepository) {
        this.notificationProducerService = notificationProducerService;
        this.challengeParticipationRepository = challengeParticipationRepository;
    }

    private static final String CHALLENGE_START_TITLE = "챌린지가 시작되었습니다!";
    private static final String CHALLENGE_START_MESSAGE_FORMAT = "%s 챌린지가 시작되었습니다.";

    private static final String CHALLENGE_END_TITLE = "챌린지가 종료되었습니다.";
    private static final String CHALLENGE_END_MESSAGE_FORMAT = "%s 챌린지가 종료되었습니다.";

    private static final String CHALLENGE_PARTICIPATION_REQUEST_TITLE = "챌린지 참여 요청이 도착했습니다.";
    private static final String CHALLENGE_PARTICIPATION_REQUEST_MESSAGE_FORMAT = "%s 님이 [%s] 챌린지에 참여를 요청했습니다.";

    private static final String CHALLENGE_PARTICIPATION_REQUEST_PROCESSED_TITLE = "챌린지 참여 요청이 처리되었습니다.";
    private static final String CHALLENGE_PARTICIPATION_REQUEST_PROCESSED_MESSAGE_FORMAT = "[%s] 챌린지에 대한 참여 신청이 %s되었습니다.";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeStatusEvent(ChallengeStatusEvent event) {
        Challenge challenge = event.getChallenge();
        List<User> participants = challengeParticipationRepository.findAllByChallengeIdAndStatus(challenge.getId(), ParticipationStatus.ACCEPTED).stream()
                .map(ChallengeParticipation::getUser)
                .toList();

        ChallengeStatusEventType type = event.getEventType();

        switch (type) {
            case START -> notifyChallengeStart(challenge, participants);
            case END -> notifyChallengeEnd(challenge, participants);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChallengeParticipationEvent(ChallengeParticipationEvent event) {
        ChallengeParticipationEventType type = event.getEventType();

        ChallengeParticipation participation = event.getParticipation();

        switch (type) {
            case PARTICIPATION_REQUEST -> notifyParticipationRequest(participation);
            case PARTICIPATION_REQUEST_PROCESSED -> notifyParticipationRequestProcessed(participation);
        }
    }

    private void notifyChallengeStart(Challenge challenge, List<User> receivers) {
        notificationProducerService.publishNotifications(
                NotificationTopics.CHALLENGE,
                receivers,
                NotificationType.CHALLENGE,
                challenge.getId(),
                CHALLENGE_START_TITLE,
                String.format(CHALLENGE_START_MESSAGE_FORMAT, challenge.getTitle())
        );
    }

    private void notifyChallengeEnd(Challenge challenge, List<User> receivers) {
        notificationProducerService.publishNotifications(
                NotificationTopics.CHALLENGE,
                receivers,
                NotificationType.CHALLENGE,
                challenge.getId(),
                CHALLENGE_END_TITLE,
                String.format(CHALLENGE_END_MESSAGE_FORMAT, challenge.getTitle())
        );
    }

    private void notifyParticipationRequest(ChallengeParticipation participation) {
        Challenge challenge = participation.getChallenge();
        User leader = getLeaderParticipation(challenge).getUser();
        User applicant = participation.getUser();

        notificationProducerService.publishNotification(
                NotificationTopics.CHALLENGE,
                leader,
                NotificationType.CHALLENGE_PARTICIPATION,
                challenge.getId(),
                CHALLENGE_PARTICIPATION_REQUEST_TITLE,
                String.format(
                        CHALLENGE_PARTICIPATION_REQUEST_MESSAGE_FORMAT,
                        applicant.getNickname(),
                        challenge.getTitle()
                )
        );
    }

    private void notifyParticipationRequestProcessed(ChallengeParticipation participation) {
        Challenge challenge = participation.getChallenge();
        User applicant = participation.getUser();

        notificationProducerService.publishNotification(
                NotificationTopics.CHALLENGE,
                applicant,
                NotificationType.CHALLENGE_PARTICIPATION,
                challenge.getId(),
                CHALLENGE_PARTICIPATION_REQUEST_PROCESSED_TITLE,
                String.format(
                        CHALLENGE_PARTICIPATION_REQUEST_PROCESSED_MESSAGE_FORMAT,
                        challenge.getTitle(),
                        participation.getStatus().getDescription()
                )
        );
    }

    private ChallengeParticipation getLeaderParticipation(Challenge challenge) {
        return challengeParticipationRepository.findByChallengeIdAndRole(challenge.getId(), ChallengeRole.LEADER)
                .orElseThrow(() -> new NotFoundChallengeParticipationException("챌린지의 리더 참여 정보를 찾을 수 없습니다."));
    }
}
