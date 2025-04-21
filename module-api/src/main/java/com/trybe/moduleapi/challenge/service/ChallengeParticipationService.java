package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.challenge.event.ChallengeEvent;
import com.trybe.moduleapi.challenge.event.ChallengeEventType;
import com.trybe.moduleapi.challenge.event.pub.ChallengeEventPublisher;
import com.trybe.moduleapi.challenge.exception.*;
import com.trybe.moduleapi.challenge.exception.participation.*;
import com.trybe.moduleapi.chat.service.ChatService;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.challenge.repository.preference.ChallengePreferenceCache;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengePreferenceCache challengePreferenceCache;
    private final ChallengeEventPublisher challengeEventPublisher;
    private final ChatService chatService;

    public ChallengeParticipationService(ChallengeParticipationRepository challengeParticipationRepository, ChallengeRepository challengeRepository, ChallengePreferenceCache challengePreferenceCache, ChallengeEventPublisher challengeEventPublisher, ChatService chatService) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeRepository = challengeRepository;
        this.challengePreferenceCache = challengePreferenceCache;
        this.challengeEventPublisher = challengeEventPublisher;
        this.chatService = chatService;
    }

    private static final int MAX_PENDING_PARTICIPATIONS = 20;

    @Transactional
    public ChallengeParticipationResponse.Detail join(User user, Long challengeId) {
        Challenge challenge = getChallenge(challengeId);
        Long userId = user.getId();

        validateDuplicatedParticipation(userId, challengeId);
        validateChallengeStatus(challenge, "챌린지가 진행 예정인 경우에만 참여 신청이 가능합니다.");
        validateChallengeCapacity(challenge);
        validateChallengeParticipationCapacity(challenge);

        ChallengeParticipation savedParticipation = challengeParticipationRepository.save(
                new ChallengeParticipation(user, challenge, ChallengeRole.MEMBER, ParticipationStatus.PENDING));
        challengePreferenceCache.addPreference(userId, challenge);
        challengeEventPublisher.publish(new ChallengeEvent(challenge, userId, ChallengeEventType.PARTICIPATION_ADD));

        return ChallengeParticipationResponse.Detail.from(savedParticipation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeParticipationResponse.Detail> getMyParticipations(User user, ParticipationStatus status, Pageable pageable) {
        Page<ChallengeParticipation> participations = challengeParticipationRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status, pageable);

        return new PageResponse<>(participations.map(ChallengeParticipationResponse.Detail::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeParticipationResponse.Summary> getParticipants(User user, Long challengeId, ParticipationStatus status, Pageable pageable) {
        Challenge challenge = getChallenge(challengeId);
        ChallengeParticipation participation = getParticipation(user.getId(), challenge.getId());

        validateParticipationStatus(participation, ParticipationStatus.ACCEPTED);
        if (status.isNot(ParticipationStatus.ACCEPTED)) {
            validateRole(participation, ChallengeRole.LEADER, "리더만 참여 신청 목록을 조회할 수 있습니다.");
        }

        Page<ChallengeParticipation> participations = challengeParticipationRepository.findAllByChallengeIdAndStatusOrderByCreatedAtAsc(challengeId, status, pageable);
        return new PageResponse<>(participations.map(ChallengeParticipationResponse.Summary::from));
    }

    @Transactional
    public ChallengeParticipationResponse.Detail confirm(User user, Long participationId, ParticipationStatus status) {
        ChallengeParticipation participation = getParticipation(participationId);
        ChallengeParticipation userParticipation = getParticipation(user.getId(), participation.getChallenge().getId());

        validateRole(userParticipation, ChallengeRole.LEADER, "리더만 참여자를 처리할 수 있습니다.");
        validateChallengeStatus(participation.getChallenge(), "챌린지가 진행 예정인 경우에만 참여 신청을 처리할 수 있습니다.");
        validateChallengeCapacity(participation.getChallenge());
        validateStatus(participation, status);

        participation.updateStatus(status);
        chatService.enter(participation.getUser(), participation.getChallenge().getId());
        return ChallengeParticipationResponse.Detail.from(participation);
    }

    @Transactional
    public void leave(User user, Long challengeId) {
        ChallengeParticipation participation = getParticipation(user.getId(), challengeId);

        validateRole(participation, ChallengeRole.MEMBER, "리더는 챌린지를 탈퇴할 수 없습니다.");

        participation.updateStatus(ParticipationStatus.DISABLED);
        chatService.exit(user, challengeId);
    }

    @Transactional
    public void cancel(User user, Long participationId) {
        ChallengeParticipation participation = getParticipation(participationId);
        Long userId = user.getId();

        validateParticipationUser(participation, userId);
        validateParticipationStatus(participation, ParticipationStatus.PENDING);

        challengeEventPublisher.publish(new ChallengeEvent(participation.getChallenge(), userId, ChallengeEventType.PARTICIPATION_REMOVE));
        challengeParticipationRepository.delete(participation);
    }

    private Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(NotFoundChallengeException::new);
    }

    private ChallengeParticipation getParticipation(Long id) {
        return challengeParticipationRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeParticipationException("존재하지 않는 챌린지 참여입니다."));
    }

    private ChallengeParticipation getParticipation(Long userId, Long challengeId) {
        return challengeParticipationRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new NotFoundChallengeParticipationException("존재하지 않는 챌린지 참여입니다."));
    }

    private void validateRole(ChallengeParticipation participation, ChallengeRole requiredRole, String message) {
        if (participation.getRole().isNot(requiredRole)) {
            throw new InvalidChallengeRoleActionException(message);
        }
    }

    private void validateParticipationStatus(ChallengeParticipation participation, ParticipationStatus requiredStatus) {
        if (participation.getStatus().isNot(requiredStatus)) {
            throw new InvalidParticipationStatusActionException("참여 상태가 " + requiredStatus.getDescription() + "인 참여자만 접근 가능합니다.");
        }
    }

    private void validateParticipationUser(ChallengeParticipation participation, Long userId) {
        if (participation.getUser().getId() != userId) {
            throw new ForbiddenParticipationException("해당 챌린지 참여 정보에 접근할 수 없습니다.");
        }
    }

    private void validateDuplicatedParticipation(Long userId, Long challengeId) {
        if (challengeParticipationRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            throw new DuplicatedChallengeParticipationException();
        }
    }

    private void validateChallengeCapacity(Challenge challenge) {
        if (challenge.getCapacity() <= challengeParticipationRepository.countByChallengeIdAndStatus(challenge.getId(), ParticipationStatus.ACCEPTED)) {
            throw new ChallengeFullException();
        }
    }

    private void validateChallengeParticipationCapacity(Challenge challenge) {
        if (MAX_PENDING_PARTICIPATIONS <= challengeParticipationRepository.countByChallengeIdAndStatus(challenge.getId(), ParticipationStatus.PENDING)) {
            throw new ChallengeParticipationFullException("참여 신청이 꽉 찼습니다.");
        }
    }

    private void validateStatus(ChallengeParticipation participation, ParticipationStatus status) {
        if (participation.getStatus().isNot(ParticipationStatus.PENDING)) {
            throw new InvalidParticipationStatusException("참여 상태가 대기 중인 참여자만 처리할 수 있습니다.");
        }

        if (status.isNot(ParticipationStatus.ACCEPTED) && status.isNot(ParticipationStatus.REJECTED)) {
            throw new InvalidParticipationStatusException("참여 수락 또는 거절만 가능합니다.");
        }
    }
  
    private void validateChallengeStatus(Challenge challenge, String message) {
        if (challenge.getStatus().isNot(ChallengeStatus.PENDING)) {
            throw new InvalidChallengeStatusException(message);
        }
    }
}
