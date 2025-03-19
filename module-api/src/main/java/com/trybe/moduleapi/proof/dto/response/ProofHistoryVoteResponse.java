package com.trybe.moduleapi.proof.dto.response;

public class ProofHistoryVoteResponse {
    public record My(
            Boolean approved
    ) { }

    public record Result(
            int approvedCount,
            int disapprovedCount,
            int nonParticipatedCount
    ) { }
}
