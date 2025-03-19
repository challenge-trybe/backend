package com.trybe.moduleapi.proof.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryVoteResponse;
import com.trybe.moduleapi.proof.service.ProofHistoryVoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proofs/histories/votes")
public class ProofHistoryVoteController {
    private final ProofHistoryVoteService proofHistoryVoteService;

    public ProofHistoryVoteController(ProofHistoryVoteService proofHistoryVoteService) {
        this.proofHistoryVoteService = proofHistoryVoteService;
    }

    @PostMapping("/{proofHistoryId}")
    public ProofHistoryVoteResponse.My save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofHistoryId") Long proofHistoryId,
            @RequestParam("approved") Boolean approved
    ) {
        return proofHistoryVoteService.save(userDetails.getUser(), proofHistoryId, approved);
    }

    @GetMapping("/{proofHistoryId}")
    public ProofHistoryVoteResponse.My findMyVote(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofHistoryId") Long proofHistoryId
    ) {
        return proofHistoryVoteService.findMyVote(userDetails.getUser(), proofHistoryId);
    }

    @GetMapping("/{proofHistoryId}/result")
    public ProofHistoryVoteResponse.Result getResult(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofHistoryId") Long proofHistoryId
    ) {
        return proofHistoryVoteService.getResult(userDetails.getUser(), proofHistoryId);
    }
}
