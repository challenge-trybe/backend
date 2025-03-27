package com.trybe.moduleapi.post.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.CommentRequest;
import com.trybe.moduleapi.post.dto.CommentResponse;
import com.trybe.moduleapi.post.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentResponse.Summary enroll(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId,
            @Valid @RequestBody CommentRequest.Enroll request){
        return commentService.enroll(userDetails.getUser(), postId, request);
    }

    @GetMapping("/comments/my")
    public PageResponse<CommentResponse.Detail> findAllByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable){
        return commentService.findAllByUser(userDetails.getUser(), pageable);
    }

    @GetMapping("/posts/{postId}/comments")
    public PageResponse<CommentResponse.Summary> findAllByPost(
            @PathVariable("postId") Long postId,
            Pageable pageable){
        return commentService.findAllByPost(postId, pageable);
    }

    @PutMapping("/comments/{commentId}")
    public CommentResponse.Summary update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody CommentRequest.Update request){
        return commentService.update(userDetails.getUser(), commentId, request);
    }

    @DeleteMapping("/comments/{commentId}")
    public void delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("commentId") Long commentId){
        commentService.delete(userDetails.getUser(), commentId);

    }
}
