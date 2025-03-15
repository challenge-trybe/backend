package com.trybe.moduleapi.proof.dto.response;

public class ProofHistoryVoteResponse {
    public record My(
            Boolean approved
    ) { }

    public record Result(
            long approvedCount,
            long disapprovedCount,
            long nonParticipatedCount
    ) { }
}
