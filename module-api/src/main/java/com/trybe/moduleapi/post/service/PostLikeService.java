package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.exception.NotFoundPostException;
import com.trybe.modulecore.post.entity.Post;
import com.trybe.modulecore.post.repository.PostRepository;
import com.trybe.modulecore.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostLikeService {
    private final String USER_REDIS_PREFIX = "user";
    private final String POST_REDIS_PREFIX = "post";
    private final RedisTemplate<String, Long> restTemplate;
    private final PostRepository postRepository;

    public PostLikeService(RedisTemplate<String, Long> restTemplate, PostRepository postRepository) {
        this.restTemplate = restTemplate;
        this.postRepository = postRepository;
    }

    public PostResponse.Like addLike(User user, Long postId) {
        validateExistPost(postId);

        String userKey = createRedisKey(USER_REDIS_PREFIX, user.getId());
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);

        if (alreadyLike(userKey, postId)) {
            return PostResponse.Like.from(count(postId),true);
        }

        double score = getCurrentTimeInSeconds();
        restTemplate.opsForZSet().add(userKey, postId, score);
        restTemplate.opsForSet().add(postKey, user.getId());
        return PostResponse.Like.from(count(postId), true);
    }

    public PostResponse.Like removedLike(User user, Long postId){
        validateExistPost(postId);

        String userKey = createRedisKey(USER_REDIS_PREFIX, user.getId());
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);

        if (!alreadyLike(userKey, postId)) {
            return PostResponse.Like.from(count(postId), false);
        }

        restTemplate.opsForZSet().remove(userKey, postId);
        restTemplate.opsForSet().remove(postKey, user.getId());
        return PostResponse.Like.from(count(postId),false);
    }

    public void removeLikesFromRedisForDeletedPost(Long postId){
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);
        Set<Long> userIds = restTemplate.opsForSet().members(postKey);
        if (userIds != null) {
            for (Long userId : userIds) {
                String userKey = createRedisKey(USER_REDIS_PREFIX, userId);
                restTemplate.opsForZSet().remove(userKey, postId);
            }
            restTemplate.delete(postKey);
        }
    }

    public int count(Long postId){
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);
        Long count = restTemplate.opsForSet().size(postKey);
        return count == null ? 0 : count.intValue();
    }

    public PageResponse<PostResponse.Summary> getLikePostByUser(User user, Pageable pageable){
        String userKey = createRedisKey(USER_REDIS_PREFIX, user.getId());

        Set<Long> postIds = restTemplate.opsForZSet().reverseRange(userKey, 0, -1);

        if (postIds == null || postIds.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }
        List<Post> posts = postRepository.findAllByIdIn(postIds);

        List<Post> filterPosts = posts.stream()
                                      .filter(post -> post.getUser().getId() != user.getId())
                                      .collect(Collectors.toList());

        List<Post> sortedPosts = postIds.stream()
                                        .map(postId -> filterPosts.stream()
                                                                  .filter(post -> post.getId().equals(postId))
                                                                  .findFirst()
                                                                  .orElseThrow(() -> new NotFoundPostException()))
                                        .collect(Collectors.toList());


        Page<Post> filterPostsPage = new PageImpl<>(sortedPosts, pageable, filterPosts.size());

        Page<PostResponse.Summary> likePostPages = filterPostsPage.map(PostResponse.Summary::from);
        
        return new PageResponse<>(likePostPages);
    }

    private String createRedisKey(String domain, Long id){
        return String.format("%s:%d", domain.toLowerCase(), id);
    }

    private void validateExistPost(Long postId){
        postRepository.findById(postId).orElseThrow(() -> new NotFoundPostException());
    }

    private boolean alreadyLike(String userKey, Long postId) {
        Double score = restTemplate.opsForZSet().score(userKey, postId);
        return score != null;
    }

    private double getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
