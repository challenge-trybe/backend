package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofHistoryRequest;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryResponse;
import com.trybe.moduleapi.proof.exception.*;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.proof.entity.Proof;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.proof.repository.ProofRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class ProofHistoryService {
    private final ProofHistoryRepository proofHistoryRepository;
    private final ProofRepository proofRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;

    public ProofHistoryService(ProofHistoryRepository proofHistoryRepository, ProofRepository proofRepository, ChallengeParticipationRepository challengeParticipationRepository) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.proofRepository = proofRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
    }

    @Transactional
    public ProofHistoryResponse.Summary save(User user, Long proofId, ProofHistoryRequest.Create request) {
        Proof proof = getProof(proofId);

        validateMemberParticipation(user.getId(), proof.getChallenge().getId(), "멤버만 인증 기록을 등록할 수 있습니다.");
        validateDate(proof);
        validateDuplicateProofHistory(proof.getId(), user.getId());

        ProofHistory savedProofHistory = proofHistoryRepository.save(request.toEntity(proof, user, request.content()));
        return ProofHistoryResponse.Summary.from(savedProofHistory);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProofHistoryResponse.Summary> findAll(User user, Long proofId, Pageable pageable) {
        Proof proof = getProof(proofId);

        validateMemberParticipation(user.getId(), proof.getChallenge().getId(), "멤버만 인증 기록을 조회할 수 있습니다.");

        Page<ProofHistory> proofHistories = proofHistoryRepository.findAllByProofId(proofId, pageable);
        return new PageResponse<>(proofHistories.map(ProofHistoryResponse.Summary::from));
    }

    @Transactional
    public ProofHistoryResponse.Summary update(User user, Long proofHistoryId, ProofHistoryRequest.Update request) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateProofHistoryOwner(user, true, proofHistory, "인증 기록의 작성자만 수정할 수 있습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록은 수정할 수 없습니다.");

        proofHistory.updateContent(request.content());

        return ProofHistoryResponse.Summary.from(proofHistory);
    }

    @Transactional
    public void delete(User user, Long proofHistoryId) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateProofHistoryOwner(user, true, proofHistory, "인증 기록의 작성자만 삭제할 수 있습니다.");

        proofHistoryRepository.delete(proofHistory);
    }

    private Proof getProof(Long proofId) {
        return proofRepository.findById(proofId)
                .orElseThrow(NotFoundProofException::new);
    }

    private ProofHistory getProofHistory(Long proofHistoryId) {
        return proofHistoryRepository.findById(proofHistoryId)
                .orElseThrow(NotFoundProofHistoryException::new);
    }

    private void validateMemberParticipation(Long userId, Long challengeId, String message) {
        if (!challengeParticipationRepository.existsByUserIdAndChallengeIdAndStatus(userId, challengeId, ParticipationStatus.ACCEPTED)) {
            throw new InvalidParticipationStatusActionException(message);
        }
    }

    private void validateProofHistoryOwner(User user, boolean shouldBe, ProofHistory proofHistory, String message) {
        if ((user.getId() != proofHistory.getUser().getId()) == shouldBe) {
            throw new ForbiddenProofHistoryException(message);
        }
    }

    private void validateDate(Proof proof) {
        if (!proof.getDate().equals(LocalDate.now())) {
            throw new InvalidProofDateException();
        }
    }

    private void validateProofHistoryStatus(ProofHistory proofHistory, ProofHistoryStatus status, String message) {
        if (proofHistory.getStatus().isNot(status)) {
            throw new InvalidProofHistoryStatusException(message);
        }
    }

    private void validateDuplicateProofHistory(Long proofId, Long userId) {
        if (proofHistoryRepository.existsByProofIdAndUserId(proofId, userId)) {
            throw new DuplicatedProofHistoryException();
        }
    }
}
