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
import com.trybe.modulecore.proof.repository.ProofHistoryRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ProofHistoryVoteService {
    private final ProofHistoryRepository proofHistoryRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final RedisTemplate<String, Long> redisTemplate;

    public ProofHistoryVoteService(ProofHistoryRepository proofHistoryRepository, ChallengeParticipationRepository challengeParticipationRepository, RedisTemplate<String, Long> redisTemplate) {
        this.proofHistoryRepository = proofHistoryRepository;
        this.challengeParticipationRepository = challengeParticipationRepository;
        this.redisTemplate = redisTemplate;
    }

    private final String USER_KEY = "user:%d";
    private final String PROOF_HISTORY_VOTES_KEY = "proofHistory:%d:votes";
    private final String PROOF_HISTORY_VOTES_APPROVED_COUNT_KEY = "proofHistory:%d:votes:approvedCount";
    private final String PROOF_HISTORY_VOTES_DISAPPROVED_COUNT_KEY = "proofHistory:%d:votes:disapprovedCount";
    private final String APPROVED = "approved";
    private final String DISAPPROVED = "disapproved";

    @Transactional
    public ProofHistoryVoteResponse.My save(User user, Long proofHistoryId, boolean approved) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateMemberParticipation(user.getId(), proofHistory.getProof().getChallenge().getId(), "챌린지 멤버만 인증 기록에 대해 투표할 수 있습니다.");
        validateProofHistoryOwner(user, false, proofHistory, "자신의 인증 기록에 투표할 수 없습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록에 대해 투표할 수 없습니다.");

        String key = getRedisKey(PROOF_HISTORY_VOTES_KEY, proofHistory.getId());
        String userKey = getRedisKey(USER_KEY, user.getId());

        validateDuplicatedVote(key, userKey);

        String approvedCountKey = getRedisKey(PROOF_HISTORY_VOTES_APPROVED_COUNT_KEY, proofHistory.getId());
        String disapprovedCountKey = getRedisKey(PROOF_HISTORY_VOTES_DISAPPROVED_COUNT_KEY, proofHistory.getId());

        if (approved) {
            redisTemplate.opsForValue().increment(approvedCountKey, 1);
        } else {
            redisTemplate.opsForValue().increment(disapprovedCountKey, 1);
        }
        redisTemplate.opsForHash().put(key, userKey, (approved ? APPROVED : DISAPPROVED));

        return new ProofHistoryVoteResponse.My(approved);
    }

    @Transactional(readOnly = true)
    public ProofHistoryVoteResponse.My findMyVote(User user, Long proofHistoryId) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateMemberParticipation(user.getId(), proofHistory.getProof().getChallenge().getId(), "챌린지 멤버만 투표 이력을 조회할 수 있습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록에 대한 투표 이력을 조회할 수 없습니다.");

        String key = getRedisKey(PROOF_HISTORY_VOTES_KEY, proofHistory.getId());
        String userKey = getRedisKey(USER_KEY, user.getId());
        String vote = (String) redisTemplate.opsForHash().get(key, userKey);

        return new ProofHistoryVoteResponse.My(vote == null ? null : vote.equals(APPROVED));
    }

    @Transactional(readOnly = true)
    public ProofHistoryVoteResponse.Result getResult(User user, Long proofHistoryId) {
        ProofHistory proofHistory = getProofHistory(proofHistoryId);

        validateProofHistoryOwner(user, true, proofHistory, "인증 기록의 작성자만 투표 결과를 조회할 수 있습니다.");
        validateProofHistoryStatus(proofHistory, ProofHistoryStatus.PENDING, "이미 처리된 인증 기록에 대한 투표 결과를 조회할 수 없습니다.");

        String approvedCountKey = getRedisKey(PROOF_HISTORY_VOTES_APPROVED_COUNT_KEY, proofHistory.getId());
        String disapprovedCountKey = getRedisKey(PROOF_HISTORY_VOTES_DISAPPROVED_COUNT_KEY, proofHistory.getId());

        int approvedCount = Optional.ofNullable(redisTemplate.opsForValue().get(approvedCountKey)).map(Long::intValue).orElse(0);
        int disapprovedCount = Optional.ofNullable(redisTemplate.opsForValue().get(disapprovedCountKey)).map(Long::intValue).orElse(0);

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

    private void validateDuplicatedVote(String key, String userKey) {
        if (redisTemplate.opsForHash().get(key, userKey) != null) {
            throw new DuplicatedProofHistoryVoteException();
        }
    }

    private String getRedisKey(String key, Long id) {
        return String.format(key, id);
    }
}
