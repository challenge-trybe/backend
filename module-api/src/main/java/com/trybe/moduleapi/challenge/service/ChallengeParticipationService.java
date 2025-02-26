package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.challenge.exception.*;
import com.trybe.moduleapi.challenge.exception.participation.*;
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
        if (challengeParticipationRepository.existsByUserIdAndChallengeId(user.getId(), challengeId)) {
            throw new DuplicatedChallengeParticipationException();
        }

        Challenge challenge = getChallenge(challengeId);

        if (MAX_PENDING_PARTICIPATIONS <= challengeParticipationRepository.countByChallengeIdAndStatus(challengeId, ParticipationStatus.PENDING)) {
            throw new ChallengeParticipationFullException("참여 신청이 꽉 찼습니다.");
        }

        if (challenge.getCapacity() <= challengeParticipationRepository.countByChallengeIdAndStatus(challengeId, ParticipationStatus.ACCEPTED)) {
            throw new ChallengeFullException();
        }

        ChallengeParticipation savedParticipation = challengeParticipationRepository.save(
                new ChallengeParticipation(user, challenge, ChallengeRole.MEMBER, ParticipationStatus.PENDING));

        return ChallengeParticipationResponse.Detail.from(savedParticipation);
    }

    public Page<ChallengeParticipationResponse.Detail> getMyParticipations(User user, ParticipationStatus status, Pageable pageable) {
        Page<ChallengeParticipation> participations = challengeParticipationRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status, pageable);

        return participations.map(ChallengeParticipationResponse.Detail::from);
    }

    public Page<ChallengeParticipationResponse.Summary> getParticipants(User user, Long challengeId, ParticipationStatus status, Pageable pageable) {
        Challenge challenge = getChallenge(challengeId);
        checkLeader(user.getId(), challenge.getId());

        Page<ChallengeParticipation> participations = challengeParticipationRepository.findAllByChallengeIdAndStatusOrderByCreatedAtAsc(challengeId, status, pageable);
        return participations.map(ChallengeParticipationResponse.Summary::from);
    }

    @Transactional
    public ChallengeParticipationResponse.Detail confirm(User user, Long participationId, ParticipationStatus status) {
        ChallengeParticipation participation = getParticipation(participationId);

        checkLeader(user.getId(), participation.getChallenge().getId());

        if (participation.getStatus() != ParticipationStatus.PENDING) {
            throw new InvalidParticipationStatusException("참여 상태가 대기 중인 참여자만 처리할 수 있습니다.");
        }

        if (status == ParticipationStatus.ACCEPTED || status == ParticipationStatus.REJECTED) {
            participation.updateStatus(status);
        } else {
            throw new InvalidParticipationStatusException("참여 수락 또는 거절만 가능합니다.");
        }

        return ChallengeParticipationResponse.Detail.from(participation);
    }

    @Transactional
    public void leave(User user, Long challengeId) {
        ChallengeParticipation participation = getParticipation(user.getId(), challengeId);

        if (participation.getRole() == ChallengeRole.LEADER) {
            throw new InvalidChallengeRoleActionException("리더는 챌린지를 탈퇴할 수 없습니다.");
        }

        participation.updateStatus(ParticipationStatus.WITHDRAWN);
    }

    private Challenge getChallenge(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeException());

        return challenge;
    }

    private ChallengeParticipation getParticipation(Long id) {
        ChallengeParticipation participation = challengeParticipationRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeParticipationException());

        return participation;
    }

    private ChallengeParticipation getParticipation(Long userId, Long challengeId) {
        ChallengeParticipation participation = challengeParticipationRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new NotFoundChallengeParticipationException());

        return participation;
    }

    private void checkLeader(Long userId, Long challengeId) {
        ChallengeParticipation participation = challengeParticipationRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new NotFoundChallengeParticipationException());

        if (participation.getRole() != ChallengeRole.LEADER) {
            throw new InvalidChallengeRoleActionException("챌린지 리더의 권한입니다.");
        }
    }
}