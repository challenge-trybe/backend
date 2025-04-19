package com.trybe.modulecore.proof.repository.vote;

import com.trybe.modulecore.proof.enums.ProofHistoryVoteStatus;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProofHistoryVoteRedisCache implements ProofHistoryVoteCache {
    private final RedisTemplate<String, Long> redisTemplate;

    public ProofHistoryVoteRedisCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final String PROOF_HISTORY_VOTES_KEY = "proofHistory:%d:votes";
    private final String PROOF_HISTORY_VOTES_APPROVED_COUNT_KEY = "proofHistory:%d:votes:approvedCount";
    private final String PROOF_HISTORY_VOTES_DISAPPROVED_COUNT_KEY = "proofHistory:%d:votes:disapprovedCount";

    @Override
    public void saveVote(Long userId, Long proofHistoryId, boolean approved) {
        String voteKey = getRedisKey(PROOF_HISTORY_VOTES_KEY, proofHistoryId);
        String countKey = approved
                ? getRedisKey(PROOF_HISTORY_VOTES_APPROVED_COUNT_KEY, proofHistoryId)
                : getRedisKey(PROOF_HISTORY_VOTES_DISAPPROVED_COUNT_KEY, proofHistoryId);
        String voteStatus = ProofHistoryVoteStatus.fromBoolean(approved).getValue();

        redisTemplate.opsForValue().increment(countKey, 1);
        redisTemplate.opsForHash().put(voteKey, userId.toString(), voteStatus);
    }

    @Override
    public String findUserVote(Long userId, Long proofHistoryId) {
        String voteKey = getRedisKey(PROOF_HISTORY_VOTES_KEY, proofHistoryId);
        return (String) redisTemplate.opsForHash().get(voteKey, userId.toString());
    }

    @Override
    public int getVoteCount(Long proofHistoryId, boolean approved) {
        String countKey = approved
                ? getRedisKey(PROOF_HISTORY_VOTES_APPROVED_COUNT_KEY, proofHistoryId)
                : getRedisKey(PROOF_HISTORY_VOTES_DISAPPROVED_COUNT_KEY, proofHistoryId);
        Long count = redisTemplate.opsForValue().get(countKey);

        return count == null ? 0 : count.intValue();
    }

    @Override
    public boolean hasUserVoted(Long userId, Long proofHistoryId) {
        String voteKey = getRedisKey(PROOF_HISTORY_VOTES_KEY, proofHistoryId);
        return redisTemplate.opsForHash().hasKey(voteKey, userId.toString());
    }

    private String getRedisKey(String key, Long id) {
        return String.format(key, id);
    }
}
