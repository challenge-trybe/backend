package com.trybe.moduleapi.proof.service;

import com.trybe.moduleapi.challenge.exception.participation.InvalidParticipationStatusActionException;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryVoteResponse;
import com.trybe.moduleapi.proof.exception.history.DuplicatedProofHistoryVoteException;
import com.trybe.moduleapi.proof.exception.history.ForbiddenProofHistoryException;
import com.trybe.moduleapi.proof.exception.history.InvalidProofHistoryStatusException;
import com.trybe.moduleapi.proof.exception.history.NotFoundProofHistoryException;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import com.trybe.modulecore.challenge.repository.ChallengeParticipationRepository;
import com.trybe.modulecore.proof.entity.ProofHistory;
import com.trybe.modulecore.proof.enums.ProofHistoryStatus;
import com.trybe.modulecore.proof.enums.ProofHistoryVoteStatus;
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.proof.repository.vote.ProofHistoryVoteCache;
import com.trybe.modulecore.user.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProofHistoryVoteService {
    private final ProofHistoryRepository proofHistoryRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ProofHistoryVoteCache proofHistoryVoteCache;

    public ProofHistoryVoteService(ProofHistoryRepository proofHistoryRepository, ChallengeParticipationRepository challengeParticipationRepository, ProofHistoryVoteCache proofHistoryVoteCache) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.proofHistoryVoteCache = proofHistoryVoteCache;
    }

    @Transactional
    public ProofHistoryVoteResponse.My save(User user, Long proofHistoryId, boolean approved) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);
        Long userId = user.getId();

        validateMemberParticipation(userId, proofHistory.getProof().getChallenge().getId(), "챌린지 멤버만 인증 기록에 대해 투표할 수 있습니다.");
        validateProofHistoryOwner(user, false, proofHistory, "자신의 인증 기록에 투표할 수 없습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록에 대해 투표할 수 없습니다.");

        validateDuplicatedVote(userId, proofHistoryId);
        proofHistoryVoteCache.saveVote(userId, proofHistoryId, approved);

        return new ProofHistoryVoteResponse.My(approved);
    }

    @Transactional(readOnly = true)
    public ProofHistoryVoteResponse.My findMyVote(User user, Long proofHistoryId) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);
        Long userId = user.getId();

        validateMemberParticipation(userId, proofHistory.getProof().getChallenge().getId(), "챌린지 멤버만 투표 이력을 조회할 수 있습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록에 대한 투표 이력을 조회할 수 없습니다.");

        String vote = proofHistoryVoteCache.findUserVote(userId, proofHistoryId);

        return new ProofHistoryVoteResponse.My(ProofHistoryVoteStatus.toBoolean(vote));
    }

    @Transactional(readOnly = true)
    public ProofHistoryVoteResponse.Result getResult(User user, Long proofHistoryId) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateProofHistoryOwner(user, true, proofHistory, "인증 기록의 작성자만 투표 결과를 조회할 수 있습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록에 대한 투표 결과를 조회할 수 없습니다.");

        int approvedCount = proofHistoryVoteCache.getVoteCount(proofHistoryId, true);
        int disapprovedCount = proofHistoryVoteCache.getVoteCount(proofHistoryId, false);

        int participantCount = challengeParticipationRepository.countByChallengeIdAndStatus(proofHistory.getProof().getChallenge().getId(), ParticipationStatus.ACCEPTED) - 1;

        return new ProofHistoryVoteResponse.Result(approvedCount, disapprovedCount, participantCount - (approvedCount + disapprovedCount));
    }

    private ProofHistory getProofHistory(Long proofHistoryId) {
        return proofHistoryRepository.findById(proofHistoryId)
                .orElseThrow(() -> new NotFoundProofHistoryException());
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

    private void validateProofHistoryStatus(ProofHistory proofHistory, ProofHistoryStatus status, String message) {
        if (proofHistory.getStatus().isNot(status)) {
            throw new InvalidProofHistoryStatusException(message);
        }
    }

    private void validateDuplicatedVote(Long userId, Long proofHistoryId) {
        if (proofHistoryVoteCache.hasUserVoted(userId, proofHistoryId)) {
            throw new DuplicatedProofHistoryVoteException();
        }
    }
}
