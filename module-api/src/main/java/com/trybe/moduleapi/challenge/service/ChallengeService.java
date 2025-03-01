package com.trybe.moduleapi.challenge.service;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.entity.ChallengeParticipation;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;

    public ChallengeService(ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository) {
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
    }

    @Transactional
    public ChallengeResponse.Detail save(User user, ChallengeRequest.Create request) {
        Challenge challenge = request.toEntity();
        Challenge savedChallenge = challengeRepository.save(challenge);

        ChallengeParticipation participation = new ChallengeParticipation(user, savedChallenge, ChallengeRole.LEADER, ParticipationStatus.ACCEPTED);
        challengeParticipationRepository.save(participation);

        return ChallengeResponse.Detail.from(savedChallenge);
    }

    @Transactional(readOnly = true)
    public ChallengeResponse.Detail find(Long id) {
        // TODO: Challenge bookmark 정보 추가 반환 (북마크 여부)
        Challenge challenge = getChallenge(id);

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional(readOnly = true)
    public Page<ChallengeResponse.Summary> findAll(ChallengeRequest.Read request, Pageable pageable) {
        // TODO: Challenge bookmark 정보 추가 반환 (북마크 여부)
        Page<Challenge> challenges = challengeRepository.findAllByStatusInAndCategoryIn(request.statuses(), request.categories(), pageable);

        return challenges.map(ChallengeResponse.Summary::from);
    }

    @Transactional
    public ChallengeResponse.Detail updateContent(User user, Long id, ChallengeRequest.UpdateContent request) {
        Challenge challenge = getChallenge(id);
        ChallengeParticipation participation = getParticipation(user.getId(), id);

        validateRole(participation, ChallengeRole.LEADER, "리더만 챌린지 정보를 수정할 수 있습니다.");
        validateChallengeStatus(challenge, true, ChallengeStatus.PENDING, "진행 예정인 챌린지만 정보를 수정할 수 있습니다.");

        challenge.updateContent(request.title(), request.description(), request.startDate(), request.endDate(), request.capacity(), request.category());

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional
    public ChallengeResponse.Detail updateProof(User user, Long id, ChallengeRequest.UpdateProof request) {
        Challenge challenge = getChallenge(id);
        ChallengeParticipation participation = getParticipation(user.getId(), id);

        validateRole(participation, ChallengeRole.LEADER, "리더만 챌린지 인증 정보를 수정할 수 있습니다.");
        validateChallengeStatus(challenge, true, ChallengeStatus.PENDING, "진행 예정인 챌린지만 인증 정보를 수정할 수 있습니다.");

        challenge.updateProof(request.proofWay(), request.proofCount());

        return ChallengeResponse.Detail.from(challenge);
    }

    @Transactional
    public void delete(User user, Long id) {
        Challenge challenge = getChallenge(id);
        ChallengeParticipation participation = getParticipation(user.getId(), id);

        validateRole(participation, ChallengeRole.LEADER, "리더만 챌린지를 삭제할 수 있습니다.");
        validateChallengeStatus(challenge, false, ChallengeStatus.ONGOING, "진행 중인 챌린지는 삭제할 수 없습니다.");

        challengeParticipationRepository.deleteAllByChallengeId(id);
        challengeRepository.delete(challenge);
    }

    private Challenge getChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundChallengeException());
    }

    private ChallengeParticipation getParticipation(Long userId, Long challengeId) {
        return challengeParticipationRepository.findByUserIdAndChallengeId(userId, challengeId)
                .orElseThrow(() -> new NotFoundChallengeException());
    }

    private void validateRole(ChallengeParticipation participation, ChallengeRole role, String message) {
        if (participation.getRole().isNot(role)) {
            throw new InvalidChallengeRoleActionException(message);
        }
    }

    private void validateChallengeStatus(Challenge challenge, boolean shouldBe, ChallengeStatus status, String message) {
        if ((shouldBe && challenge.getStatus().isNot(status)) || (!shouldBe && challenge.getStatus().is(status))) {
            throw new InvalidChallengeStatusException(message);
        }
    }
}