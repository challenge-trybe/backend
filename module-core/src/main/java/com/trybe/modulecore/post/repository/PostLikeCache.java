package com.trybe.modulecore.post.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Repository
public class PostLikeCache {
    private final RedisTemplate<String, Long> redisTemplate;

    private final String POST_LIKE_SUFFIX = ":liked";
    private final String USER_REDIS_KEY = "user:%d" + POST_LIKE_SUFFIX + ":posts";
    private final String POST_REDIS_KEY = "post:%d" + POST_LIKE_SUFFIX + ":users";
    private final String POST_LIKE_DELETE_KEY = POST_REDIS_KEY + ":deleted";


    public PostLikeCache(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int addLike(Long userId, Long postId){
        String userKey = createRedisKey(USER_REDIS_KEY, userId);
        String deleteKey = createRedisKey(POST_LIKE_DELETE_KEY, postId);
        String postKey = createRedisKey(POST_REDIS_KEY, postId);
        int likeCount = getPostLikeCount(postId);

        if (!alreadyLike(userKey, postId)) {
            double score = getCurrentTimeInSeconds();
            redisTemplate.opsForZSet().add(userKey, postId, score);
            redisTemplate.opsForSet().add(postKey, userId);
            redisTemplate.opsForSet().remove(deleteKey, userId);
            likeCount++;
        }
        return likeCount;
    }

    public int removeLike(Long userId, Long postId){
        String userKey = createRedisKey(USER_REDIS_KEY, userId);
        String deleteKey = createRedisKey(POST_LIKE_DELETE_KEY, postId);
        String postKey = createRedisKey(POST_REDIS_KEY, postId);

        int likeCount = getPostLikeCount(postId);

        if (alreadyLike(userKey, postId)) {
            redisTemplate.opsForZSet().remove(userKey, postId);
            redisTemplate.opsForSet().remove(postKey, userId);
            redisTemplate.opsForSet().add(deleteKey, userId);
            likeCount--;
        }
        return likeCount;
    }

    public void removeLikesByPost(Long postId){
        String postKey = createRedisKey(POST_REDIS_KEY, postId);
        Set<Long> userIds = redisTemplate.opsForSet().members(postKey);
        if (userIds != null) {
            for (Long userId : userIds) {
                String userKey = createRedisKey(USER_REDIS_KEY, userId);
                redisTemplate.opsForZSet().remove(userKey, postId);

                String deleteKey = createRedisKey(POST_LIKE_DELETE_KEY, postId);
                redisTemplate.opsForSet().add(deleteKey, userId);
            }
            redisTemplate.delete(postKey);
        }
    }

    public Set<Long> getLikePostIdsByUser(Long userId, int start, int end){
        String userKey = createRedisKey(USER_REDIS_KEY, userId);
        return Optional.ofNullable(redisTemplate.opsForZSet().reverseRange(userKey, start, end))
                .orElse(Collections.emptySet());
    }

    public int getUserPostLikeCount(Long userId){
        String userKey = createRedisKey(USER_REDIS_KEY, userId);
        Long count = redisTemplate.opsForZSet().size(userKey);
        return count == null ? 0 : count.intValue();
    }

    public int getPostLikeCount(Long postId){
        String postKey = createRedisKey(POST_REDIS_KEY, postId);
        Long count = redisTemplate.opsForSet().size(postKey);
        return count == null ? 0 : count.intValue();
    }

    private String createRedisKey(String key, Long id){
        return String.format(key, id);
    }

    private boolean alreadyLike(String userKey, Long postId) {
        Double score = redisTemplate.opsForZSet().score(userKey, postId);
        return score != null;
    }

    private double getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
