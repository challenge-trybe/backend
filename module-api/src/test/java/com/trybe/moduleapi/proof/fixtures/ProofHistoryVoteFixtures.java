package com.trybe.moduleapi.proof.fixtures;

import com.trybe.moduleapi.proof.dto.response.ProofHistoryVoteResponse;

public class ProofHistoryVoteFixtures {
    public static final boolean 찬성_여부 = true;
    public static final int 찬성_투표_수 = 3;
    public static final int 반대_투표_수 = 2;
    public static final int 투표_미참여_인원_수 = 1;

    /* Response DTO */
    public static final ProofHistoryVoteResponse.My 나의_투표_응답 = new ProofHistoryVoteResponse.My(찬성_여부);
    public static final ProofHistoryVoteResponse.Result 투표_결과_응답 = new ProofHistoryVoteResponse.Result(찬성_투표_수, 반대_투표_수, 투표_미참여_인원_수);
}