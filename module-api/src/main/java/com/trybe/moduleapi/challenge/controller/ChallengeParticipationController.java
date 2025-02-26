package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.challenge.dto.ChallengeParticipationResponse;
import com.trybe.moduleapi.challenge.service.ChallengeParticipationService;
import com.trybe.modulecore.challenge.enums.ParticipationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/challenges/participations")
public class ChallengeParticipationController {
    private final ChallengeParticipationService challengeParticipationService;

    public ChallengeParticipationController(ChallengeParticipationService challengeParticipationService) {
        this.challengeParticipationService = challengeParticipationService;
    }

    @PostMapping("/{challengeId}")
    public ChallengeParticipationResponse.Detail join(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("challengeId") Long challengeId
    ) {
        return challengeParticipationService.join(userDetails.getUser(), challengeId);
    }

    @GetMapping("/my")
    public Page<ChallengeParticipationResponse.Detail> getMyParticipations(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("status") ParticipationStatus status,
            Pageable pageable
    ) {
        return challengeParticipationService.getMyParticipations(userDetails.getUser(), status, pageable);
    }

    @GetMapping("/{challengeId}")
    public Page<ChallengeParticipationResponse.Summary> getParticipants(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("challengeId") Long challengeId,
            @RequestParam("status") ParticipationStatus status,
            Pageable pageable
    ) {
        return challengeParticipationService.getParticipants(userDetails.getUser(), challengeId, status, pageable);
    }

    @PutMapping("/confirm/{participationId}")
    public ChallengeParticipationResponse.Detail confirm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("participationId") Long participationId,
            @RequestParam("status") ParticipationStatus status
    ) {
        return challengeParticipationService.confirm(userDetails.getUser(), participationId, status);
    }

    @PutMapping("/leave/{participationId}")
    public void leave(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("participationId") Long participationId
    ) {
        challengeParticipationService.leave(userDetails.getUser(), participationId);
    }
}
