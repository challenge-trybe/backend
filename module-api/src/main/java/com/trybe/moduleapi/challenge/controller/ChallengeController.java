package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.service.ChallengeService;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.modulecore.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart("request") @Valid ChallengeRequest.Create request
    ) {
        return challengeService.save(userDetails.getUser(), thumbnail, request);
    }

    @GetMapping("/{id}")
    public ChallengeResponse.Detail find(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id
    ) {
        User user = userDetails == null ? null : userDetails.getUser();
        return challengeService.find(user, id);
    }

    @PostMapping("/search")
    public PageResponse<ChallengeResponse.Preview> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChallengeRequest.Read request,
            Pageable pageable
    ) {
        User user = userDetails == null ? null : userDetails.getUser();
        return challengeService.findAll(user, request, pageable);
    }

    @GetMapping("/popular")
    public List<ChallengeResponse.Preview> getPopular(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails == null ? null : userDetails.getUser();
        return challengeService.getPopular(user);
    }

    @GetMapping("/recommendations")
    public List<ChallengeResponse.Preview> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return challengeService.getRecommendations(userDetails.getUser());
    }

    @PutMapping("/{id}/content")
    public ChallengeResponse.Detail updateContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("id") Long id,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestPart("request") @Valid ChallengeRequest.UpdateContent request
    ) {
        return challengeService.updateContent(userDetails.getUser(), id, thumbnail, request);
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
