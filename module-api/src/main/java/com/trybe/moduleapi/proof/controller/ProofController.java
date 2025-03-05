package com.trybe.moduleapi.proof.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofRequest;
import com.trybe.moduleapi.proof.dto.response.ProofResponse;
import com.trybe.moduleapi.proof.service.ProofService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/proofs")
public class ProofController {
    private final ProofService proofService;

    public ProofController(ProofService proofService) {
        this.proofService = proofService;
    }

    @PostMapping
    public ProofResponse.Summary save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProofRequest.Create request
    ) {
        return proofService.save(userDetails.getUser(), request);
    }

    @GetMapping("/{proofId}")
    public ProofResponse.Summary find(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofId") Long proofId
    ) {
        return proofService.find(userDetails.getUser(), proofId);
    }

    @GetMapping("/challenge/{challengeId}")
    public PageResponse<ProofResponse.Summary> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("challengeId") Long challengeId,
            Pageable pageable
    ) {
        return proofService.findAll(userDetails.getUser(), challengeId, pageable);
    }

    @DeleteMapping("/{proofId}")
    public void delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofId") Long proofId
    ) {
        proofService.delete(userDetails.getUser(), proofId);
    }
}
