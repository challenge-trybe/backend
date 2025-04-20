package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.PostLikeCache;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostLikeService {
    private final PostRepository postRepository;
    private final PostLikeCache postLikeCache;

    public PostLikeService(PostRepository postRepository, PostLikeCache postLikeCache) {
        this.postRepository = postRepository;
        this.postLikeCache = postLikeCache;
    }

    @Transactional
    public PostResponse.Like addLike(User user, Long postId) {
        validateExistPost(postId);

        int likeCount = postLikeCache.getPostLikeCount(postId);
        if (!postLikeCache.alreadyLike(user.getId(), postId)){
            postLikeCache.addLike(user.getId(), postId);
            likeCount++;
        }
        return PostResponse.Like.from(likeCount, true);
    }

    @Transactional
    public PostResponse.Like removeLike(User user, Long postId){
        validateExistPost(postId);

        int likeCount = postLikeCache.getPostLikeCount(postId);
        if (postLikeCache.alreadyLike(user.getId(), postId)){
            postLikeCache.removeLike(user.getId(), postId);
            likeCount--;
        }
        return PostResponse.Like.from(likeCount,false);
    }

    public int getPostLikeCount(Long postId){
        return postLikeCache.getPostLikeCount(postId);
    }

    public void removeLikesByPost(Long postId){
        postLikeCache.removeLikesByPost(postId);
    }

    @Transactional
    public PageResponse<PostResponse.Summary> getLikePostByUser(User user, Pageable pageable){
        int start = pageable.getPageNumber() * pageable.getPageSize();
        int end = start + pageable.getPageSize() - 1;

        Set<Long> postIds = postLikeCache.getLikePostIdsByUser(user.getId(), start, end);

        List<Post> posts = postIds.isEmpty() ? Collections.emptyList() : postRepository.findAllByIdIn(postIds);
        List<Post> sortedPosts = sortPosts(posts, postIds);

        int totalElements = postLikeCache.getUserPostLikeCount(user.getId());

        Page<Post> postPage = new PageImpl<>(sortedPosts, pageable, totalElements);
        Page<PostResponse.Summary> likePostPages = postPage.map(PostResponse.Summary::from);
        return new PageResponse<>(likePostPages);
    }

    private void validateExistPost(Long postId){
        if (!postRepository.existsById(postId)) {
            throw new NotFoundPostException();
        }
    }

    private List<Post> sortPosts(List<Post> posts, Set<Long> postIds) {
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, post -> post));

        return postIds.stream()
                .map(postMap::get)
                .toList();
    }
}
