package com.trybe.moduleapi.post.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.service.PostLikeService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts/likes")
public class PostLikeController {
    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/{postId}")
    public PostResponse.Like addLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId
    ) {
        return postLikeService.addLike(userDetails.getUser(), postId);
    }

    @DeleteMapping("/{postId}")
    public PostResponse.Like removeLike(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("postId") Long postId
    ){
        return postLikeService.removeLike(userDetails.getUser(), postId);
    }

    @GetMapping("/my")
    public PageResponse<PostResponse.Summary> getLikePostsByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable
    ){
        return postLikeService.getLikePostByUser(userDetails.getUser(), pageable);
    }
}
