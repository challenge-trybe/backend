package com.trybe.moduleapi.proof.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.proof.dto.request.ProofHistoryRequest;
import com.trybe.moduleapi.proof.dto.response.ProofHistoryResponse;
import com.trybe.moduleapi.proof.service.ProofHistoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/proofs/histories")
public class ProofHistoryController {
    private final ProofHistoryService proofHistoryService;

    public ProofHistoryController(ProofHistoryService proofHistoryService) {
        this.proofHistoryService = proofHistoryService;
    }

    @PostMapping("/{proofId}")
    public ProofHistoryResponse.Summary save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofId") Long proofId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("request") @Valid ProofHistoryRequest.Create request
    ) {
        return proofHistoryService.save(userDetails.getUser(),proofId, files, request);
    }

    @GetMapping("/all/{proofId}")
    public PageResponse<ProofHistoryResponse.Summary> findAll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofId") Long proofId,
            Pageable pageable
    ) {
        return proofHistoryService.findAll(userDetails.getUser(), proofId, pageable);
    }

    @PutMapping("/{proofHistoryId}")
    public ProofHistoryResponse.Summary update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofHistoryId") Long proofHistoryId,
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("request") @Valid ProofHistoryRequest.Update request
    ) {
        return proofHistoryService.update(userDetails.getUser(), proofHistoryId, files, request);
    }

    @DeleteMapping("/{proofHistoryId}")
    public void delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("proofHistoryId") Long proofHistoryId
    ) {
        proofHistoryService.delete(userDetails.getUser(), proofHistoryId);
    }
}