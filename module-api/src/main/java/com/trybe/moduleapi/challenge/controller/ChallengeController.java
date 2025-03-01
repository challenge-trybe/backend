package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.service.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @PostMapping
    public ChallengeResponse.Detail save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChallengeRequest.Create request
    ) {
        return challengeService.save(userDetails.getUser(), request);
    }

    @GetMapping("/{id}")
    public ChallengeResponse.Detail find(@PathVariable("id") Long id) {
        return challengeService.find(id);
    }

    @PostMapping("/search")
    public Page<ChallengeResponse.Summary> findAll(
            @Valid @RequestBody ChallengeRequest.Read request,
            Pageable pageable
    ) {
        return challengeService.findAll(request, pageable);
    }

    @PutMapping("/{id}/content")
    public ChallengeResponse.Detail updateContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @RequestBody ChallengeRequest.UpdateContent request
    ) {
        return challengeService.updateContent(userDetails.getUser(), id, request);
    }

    @PutMapping("/{id}/proof")
    public ChallengeResponse.Detail updateProof(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @Valid @RequestBody ChallengeRequest.UpdateProof request
    ) {
        return challengeService.updateProof(userDetails.getUser(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id
    ) {
        challengeService.delete(userDetails.getUser(), id);
    }
}
