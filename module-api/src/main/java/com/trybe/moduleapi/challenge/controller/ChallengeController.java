package com.trybe.moduleapi.challenge.controller;

import com.trybe.moduleapi.challenge.dto.ChallengeRequest;
import com.trybe.moduleapi.challenge.dto.ChallengeResponse;
import com.trybe.moduleapi.challenge.service.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    private final Long TMP_USER_ID = 1L;
    // TODO: Authentication 적용

    @PostMapping
    public ChallengeResponse.Detail save(@Valid @RequestBody ChallengeRequest.Create request) {
        return challengeService.save(request, TMP_USER_ID);
    }

    @GetMapping("/{id}")
    public ChallengeResponse.Detail find(@PathVariable("id") Long id) {
        return challengeService.find(id);
    }

    @PostMapping("/search")
    public List<ChallengeResponse.Summary> findAll(@Valid @RequestBody ChallengeRequest.Read request) {
        return challengeService.findAll(request);
    }

    @PutMapping("/{id}/content")
    public ChallengeResponse.Detail updateContent(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChallengeRequest.UpdateContent request
    ) {
        return challengeService.updateContent(id, request, TMP_USER_ID);
    }

    @PutMapping("/{id}/proof")
    public ChallengeResponse.Detail updateProof(
            @PathVariable("id") Long id,
            @Valid @RequestBody ChallengeRequest.UpdateProof request
    ) {
        return challengeService.updateProof(id, request, TMP_USER_ID);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        challengeService.delete(id, TMP_USER_ID);
    }
}
