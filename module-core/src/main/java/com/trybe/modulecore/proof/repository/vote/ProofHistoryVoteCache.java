package com.trybe.modulecore.proof.repository.vote;

public interface ProofHistoryVoteCache {
    void saveVote(Long userId, Long proofHistoryId, boolean approved);
    String findUserVote(Long userId, Long proofHistoryId);
    int getVoteCount(Long proofHistoryId, boolean approved);
    boolean hasUserVoted(Long userId, Long proofHistoryId);
}
