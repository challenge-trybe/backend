package com.trybe.moduleapi.post.controller;

import com.trybe.moduleapi.auth.CustomUserDetails;
import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostRequest;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.service.PostService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public PostResponse.Detail save(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @Valid @RequestBody PostRequest.Create request) {
        return postService.save(userDetails.getUser(), request);
    }

    @GetMapping("/{id}")
    public PostResponse.Detail findById(@PathVariable Long id) {
        return postService.find(id);
    }

    // 전체 조회 (정렬/필터링이 없는 경우 + 정렬/필터링이 있는 경우)
    @PostMapping("/search")
    public PageResponse<PostResponse.Summary> findAll(@Valid @RequestBody PostRequest.Read request,
                                                     Pageable pageable){
        return postService.findAll(request, pageable);
    }

    @PutMapping("/{id}")
    public PostResponse.Detail update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long id,
                                      @Valid @RequestBody PostRequest.Update request){
        return postService.updatePost(userDetails.getUser(), id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                       @PathVariable Long id) {
        postService.delete(userDetails.getUser(), id);
    }
}
