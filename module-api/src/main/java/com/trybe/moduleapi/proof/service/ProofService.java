package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.InvalidChallengeStatusException;
import com.trybe.moduleapi.challenge.exception.NotFoundChallengeException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidChallengeRoleActionException;
import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofRequest;
import com.trybe.moduleapi.proof.dto.response.ProofResponse;
import com.trybe.moduleapi.proof.exception.DuplicatedProofException;
import com.trybe.moduleapi.proof.exception.InvalidProofDeletionException;
import com.trybe.moduleapi.proof.exception.NotFoundProofException;
import com.trybe.moduleapi.proof.exception.ProofCountExceededException;
import com.trybe.modulecore.challenge.entity.Challenge;
import com.trybe.modulecore.challenge.enums.ChallengeRole;
import com.trybe.modulecore.challenge.enums.ChallengeStatus;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.challenge.repository.ChallengeRepository;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.repository.ProofRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ProofService {
    private final ProofRepository proofRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;

    public ProofService(ProofRepository proofRepository, ChallengeRepository challengeRepository, ChallengeParticipationRepository challengeParticipationRepository) {
        this.proofRepository = proofRepository;
        this.challengeRepository = challengeRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
    }

    @Transactional
    public ProofResponse.Summary save(User user, ProofRequest.Create request) {
        Challenge challenge = getChallenge(request.challengeId());

        validateLeaderParticipation(user.getId(), challenge.getId(), "리더만 인증을 등록할 수 있습니다.");
        validateChallengeStatus(challenge, ChallengeStatus.ONGOING, "진행 중인 챌린지만 인증을 등록할 수 있습니다.");
        validateDuplicateProof(challenge.getId(), request.date());

        int round = proofRepository.countByChallengeId(challenge.getId()) + 1;

        validateProofCount(challenge, round);

        Proof savedProof = proofRepository.save(new Proof(challenge, request.date(), round));

        return ProofResponse.Summary.from(savedProof);
    }

    @Transactional(readOnly = true)
    public ProofResponse.Summary find(User user, Long proofId) {
        Proof proof = getProof(proofId);

        validateMemberParticipation(user.getId(), proof.getChallenge().getId(), "참여자만 인증을 조회할 수 있습니다.");

        return ProofResponse.Summary.from(proof);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProofResponse.Summary> findAll(User user, Long challengeId, Pageable pageable) {
        Challenge challenge = getChallenge(challengeId);

        validateMemberParticipation(user.getId(), challenge.getId(), "참여자만 인증 목록을 조회할 수 있습니다.");

        return new PageResponse<>(proofRepository.findAllByChallengeId(challengeId, pageable).map(ProofResponse.Summary::from));
    }

    @Transactional
    public void delete(User user, Long proofId) {
        Proof proof = getProof(proofId);

        validateLeaderParticipation(user.getId(), proof.getChallenge().getId(), "리더만 인증을 삭제할 수 있습니다.");
        validateTimeConstraint(proof.getDate(), 1L);

        proofRepository.delete(proof);
    }

    private Proof getProof(Long proofId) {
        return proofRepository.findById(proofId)
                .orElseThrow(() -> new NotFoundProofException());
    }

    private Challenge getChallenge(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundChallengeException());
    }

    private void validateChallengeStatus(Challenge challenge, ChallengeStatus status, String message) {
        if (challenge.getStatus() != status) {
            throw new InvalidChallengeStatusException(message);
        }
    }

    private void validateMemberParticipation(Long userId, Long challengeId, String message) {
        if (!challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(userId, challengeId, ParticipationStatus.ACCEPTED)) {
            throw new InvalidParticipationStatusActionException(message);
        }
    }

    private void validateLeaderParticipation(Long userId, Long challengeId, String message) {
        if (!challengeParticipationRepository.existsByUserIdAndChallengeIdAndRole(userId, challengeId, ChallengeRole.LEADER)) {
            throw new InvalidChallengeRoleActionException(message);
        }
    }

    private void validateTimeConstraint(LocalDate date, Long hoursBefore) {
        LocalDateTime dateTime = date.atTime(0, 0);
        if (dateTime.isBefore(LocalDateTime.now().minusHours(hoursBefore))) {
            throw new InvalidProofDeletionException("인증 삭제는 시작하기 " + hoursBefore + "시간 이내에만 가능합니다.");
        }
    }

    private void validateDuplicateProof(Long challengeId, LocalDate date) {
        if (proofRepository.existsByChallengeIdAndDate(challengeId, date)) {
            throw new DuplicatedProofException();
        }
    }

    private void validateProofCount(Challenge challenge, int round) {
        if (round > challenge.getProofCount()) {
            throw new ProofCountExceededException(challenge.getProofCount());
        }
    }
}
