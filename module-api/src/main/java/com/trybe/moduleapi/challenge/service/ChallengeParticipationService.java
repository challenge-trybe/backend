package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.challenge.exception.*;
import com.trybe.moduleapi.challenge.exception.participation.*;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeRepository challengeRepository;

    public ChallengeParticipationService(ChallengeParticipationRepository challengeParticipationRepository, ChallengeRepository challengeRepository) {
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.challengeRepository = challengeRepository;
    }

    private static final int MAX_PENDING_PARTICIPATIONS = 20;

    @Transactional
    public ChallengeParticipationResponse.Detail join(User user, Long challengeId) {
        validateDuplicatedParticipation(user.getId(), challengeId);

        Challenge challenge = getChallenge(challengeId);

        validateCapacity(challenge);

        ChallengeParticipation savedParticipation = challengeParticipationRepository.save(
                new ChallengeParticipation(user, challenge, ChallengeRole.MEMBER, ParticipationStatus.PENDING));

        return ChallengeParticipationResponse.Detail.from(savedParticipation);
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeParticipationResponse.Detail> getMyParticipations(User user, ParticipationStatus status, Pageable pageable) {
        Page<ChallengeParticipation> participations = challengeParticipationRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status, pageable);

        return new PageResponse<>(participations.map(ChallengeParticipationResponse.Detail::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ChallengeParticipationResponse.Summary> getParticipants(User user, Long challengeId, ParticipationStatus status, Pageable pageable) {
        ChallengeParticipation participation = getParticipation(user.getId(), challengeId);
        checkRole(participation, ChallengeRole.LEADER, "리더만 참여자 목록을 조회할 수 있습니다.");

        Page<ChallengeParticipation> participations = challengeParticipationRepository.findAllByChallengeIdAndStatusOrderByCreatedAtAsc(challengeId, status, pageable);
        return new PageResponse<>(participations.map(ChallengeParticipationResponse.Summary::from));
    }

    @Transactional
    public ChallengeParticipationResponse.Detail confirm(User user, Long participationId, ParticipationStatus status) {
        ChallengeParticipation participation = getParticipation(participationId);

        checkRole(participation, ChallengeRole.LEADER, "리더만 참여자를 처리할 수 있습니다.");
        validateStatus(participation, status);

        participation.updateStatus(status);

        return ChallengeParticipationResponse.Detail.from(participation);
    }

    @Transactional
    public void leave(User user, Long challengeId) {
        ChallengeParticipation participation = getParticipation(user.getId(), challengeId);

        checkRole(participation, ChallengeRole.MEMBER, "리더는 챌린지를 탈퇴할 수 없습니다.");

        participation.updateStatus(ParticipationStatus.DISABLED);
    }

    private Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeException());
    }

    private ChallengeParticipation getParticipation(Long id) {
        return challengeParticipationRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeParticipationException("존재하지 않는 챌린지 참여입니다."));
    }

    private ChallengeParticipation getParticipation(Long userId, Long challengeId) {
        return challengeParticipationRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new NotFoundChallengeParticipationException("존재하지 않는 챌린지 참여입니다."));
    }

    private void checkRole(ChallengeParticipation participation, ChallengeRole requiredRole, String message) {
        if (participation.getRole().isNot(requiredRole)) {
            throw new InvalidChallengeRoleActionException(message);
        }
    }

    private void validateDuplicatedParticipation(Long userId, Long challengeId) {
        if (challengeParticipationRepository.existsByUserIdAndChallengeId(userId, challengeId)) {
            throw new DuplicatedChallengeParticipationException();
        }
    }

    private void validateCapacity(Challenge challenge) {
        if (MAX_PENDING_PARTICIPATIONS <= challengeParticipationRepository.countByChallengeIdAndStatus(challenge.getId(), ParticipationStatus.PENDING)) {
            throw new ChallengeParticipationFullException("참여 신청이 꽉 찼습니다.");
        }

        if (challenge.getCapacity() <= challengeParticipationRepository.countByChallengeIdAndStatus(challenge.getId(), ParticipationStatus.ACCEPTED)) {
            throw new ChallengeFullException();
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
}
