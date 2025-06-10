package com.trybe.moduleapi.proof.event.listener;

import com.trybe.moduleapi.notification.constants.NotificationTopics;
import com.trybe.moduleapi.notification.service.NotificationProducerService;
import com.trybe.moduleapi.proof.event.model.ProofEvent;
import com.trybe.moduleapi.proof.event.model.ProofHistoryEvent;
import com.trybe.moduleapi.proof.event.type.ProofEventType;
import com.trybe.moduleapi.proof.event.type.ProofHistoryEventType;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.notification.enums.NotificationType;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.user.entity.User;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class ProofNotificationEventListener {
    private final NotificationProducerService notificationProducerService;

    public ProofNotificationEventListener(NotificationProducerService notificationProducerService) {
        this.notificationProducerService = notificationProducerService;
    }

    private static final String PROOF_START_TITLE = "인증이 시작되었습니다!";
    private static final String PROOF_START_MESSAGE_FORMAT = "%s 챌린지의 %d번째 인증이 시작되었습니다.";

    private static final String PROOF_END_TITLE = "인증이 종료되었습니다.";
    private static final String PROOF_END_MESSAGE_FORMAT = "%s 챌린지의 인증이 종료되었습니다. 투표에 참여하세요!";

    private static final String PROOF_HISTORY_CREATED_TITLE = "인증 기록이 등록되었습니다.";
    private static final String PROOF_HISTORY_CREATED_MESSAGE_FORMAT = "%s 님이 %s 챌린지의 인증 기록을 등록했습니다.";

    private static final String PROOF_HISTORY_VOTE_END_TITLE = "인증 기록 투표가 종료되었습니다.";
    private static final String PROOF_HISTORY_VOTE_END_MESSAGE_FORMAT = "%s 챌린지의 인증 기록 투표가 종료되었습니다. 투표 결과를 확인하세요!";

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProofEvent(ProofEvent event) {
        Proof proof = event.getProof();
        ProofEventType type = event.getEventType();
        List<User> participants = event.getParticipants();

        switch (type) {
            case START -> notifyProofStart(proof, participants);
            case END -> notifyProofEnd(proof, participants);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProofHistoryEvent(ProofHistoryEvent event) {
        ProofHistory proofHistory = event.getProofHistory();
        ProofHistoryEventType type = event.getEventType();
        List<User> participants = event.getParticipants();

        switch (type) {
            case CREATED -> notifyProofHistoryCreated(proofHistory, participants);
            case VOTE_END -> notifyProofHistoryVoteEnd(proofHistory);
        }
    }

    private void notifyProofStart(Proof proof, List<User> participants) {
        Challenge challenge = proof.getChallenge();
        String message = String.format(PROOF_START_MESSAGE_FORMAT, challenge.getTitle(), proof.getRound());

        notificationProducerService.publishNotifications(
                NotificationTopics.CHALLENGE_PROOF,
                participants,
                NotificationType.PROOF,
                proof.getId(),
                PROOF_START_TITLE,
                message
        );
    }

    private void notifyProofEnd(Proof proof, List<User> participants) {
        Challenge challenge = proof.getChallenge();
        String message = String.format(PROOF_END_MESSAGE_FORMAT, challenge.getTitle());

        notificationProducerService.publishNotifications(
                NotificationTopics.CHALLENGE_PROOF,
                participants,
                NotificationType.PROOF,
                proof.getId(),
                PROOF_END_TITLE,
                message
        );
    }

    private void notifyProofHistoryCreated(ProofHistory proofHistory, List<User> participants) {
        Proof proof = proofHistory.getProof();
        Challenge challenge = proof.getChallenge();
        User writer = proofHistory.getUser();

        List<User> receivers = participants.stream()
                .filter(user -> user.getId().equals(writer.getId()))
                .toList();

        String message = String.format(PROOF_HISTORY_CREATED_MESSAGE_FORMAT, writer.getNickname(), challenge.getTitle());

        notificationProducerService.publishNotifications(
                NotificationTopics.CHALLENGE_PROOF_HISTORY,
                receivers,
                NotificationType.PROOF_HISTORY,
                proof.getId(),
                PROOF_HISTORY_CREATED_TITLE,
                message
        );
    }

    private void notifyProofHistoryVoteEnd(ProofHistory proofHistory) {
        Proof proof = proofHistory.getProof();
        Challenge challenge = proof.getChallenge();
        User writer = proofHistory.getUser();

        String message = String.format(PROOF_HISTORY_VOTE_END_MESSAGE_FORMAT, challenge.getTitle());

        notificationProducerService.publishNotification(
                NotificationTopics.CHALLENGE_PROOF_HISTORY,
                writer,
                NotificationType.PROOF_HISTORY,
                proof.getId(),
                PROOF_HISTORY_VOTE_END_TITLE,
                message
        );
    }
}
