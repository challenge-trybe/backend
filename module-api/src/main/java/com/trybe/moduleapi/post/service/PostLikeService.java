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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostLikeService {
    private final String POST_LIKE_SUFFIX = ":liked";
    private final String USER_REDIS_KEY = "user:%d" + POST_LIKE_SUFFIX;
    private final String POST_REDIS_KEY = "post:%d" + POST_LIKE_SUFFIX;
    private final String POST_LIKE_DELETE_KEY = POST_REDIS_KEY + ":deleted";
    private final RedisTemplate<String, Long> redisTemplate;
    private final PostRepository postRepository;

    public PostLikeService(RedisTemplate<String, Long> redisTemplate, PostRepository postRepository) {
        this.redisTemplate = redisTemplate;
        this.postRepository = postRepository;
    }

    @Transactional
    public PostResponse.Like addLike(User user, Long postId) {
        validateExistPost(postId);

        String userKey = createRedisKey(USER_REDIS_KEY, user.getId());
        String deleteKey = createRedisKey(POST_LIKE_DELETE_KEY, postId);
        String postKey = createRedisKey(POST_REDIS_KEY, postId);
        int likeCount = count(postId);

        if (!alreadyLike(userKey, postId)) {
            double score = getCurrentTimeInSeconds();
            redisTemplate.opsForZSet().add(userKey, postId, score);
            redisTemplate.opsForSet().add(postKey, user.getId());
            redisTemplate.opsForSet().remove(deleteKey, user.getId());
            likeCount++;
        }
        return PostResponse.Like.from(likeCount, true);
    }

    @Transactional
    public PostResponse.Like removeLike(User user, Long postId){
        validateExistPost(postId);

        String userKey = createRedisKey(USER_REDIS_KEY, user.getId());
        String deleteKey = createRedisKey(POST_LIKE_DELETE_KEY, postId);
        String postKey = createRedisKey(POST_REDIS_KEY, postId);

        int likeCount = count(postId);

        if (alreadyLike(userKey, postId)) {
            redisTemplate.opsForZSet().remove(userKey, postId);
            redisTemplate.opsForSet().remove(postKey, user.getId());
            redisTemplate.opsForSet().add(deleteKey, user.getId());
            likeCount--;
        }
        return PostResponse.Like.from(likeCount,false);
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

    public int count(Long postId){
        String postKey = createRedisKey(POST_REDIS_KEY, postId);
        Long count = redisTemplate.opsForSet().size(postKey);
        return count == null ? 0 : count.intValue();
    }

    @Transactional
    public PageResponse<PostResponse.Summary> getLikePostByUser(User user, Pageable pageable){
        String userKey = createRedisKey(USER_REDIS_KEY, user.getId());

        Set<Long> postIds = redisTemplate.opsForZSet().reverseRange(userKey, 0, -1);

        if (postIds == null || postIds.isEmpty()) {
            return new PageResponse<>(Page.empty());
        }
        List<Post> posts = postRepository.findAllByIdIn(postIds);
        List<Post> sortedPosts = postIds.stream()
                                        .map(postId -> posts.stream()
                                                                  .filter(post -> postId.equals(post.getId()))
                                                                  .findFirst()
                                                                  .orElse(null))
                                        .filter(Objects::nonNull)
                                        .collect(Collectors.toList());


        Page<Post> filterPostsPage = new PageImpl<>(sortedPosts, pageable, sortedPosts.size());
        Page<PostResponse.Summary> likePostPages = filterPostsPage.map(PostResponse.Summary::from);
        return new PageResponse<>(likePostPages);
    }

    private String createRedisKey(String key, Long id){
        return String.format(key, id);
    }

    private void validateExistPost(Long postId){
        if (!postRepository.existsById(postId)) {
            throw new NotFoundPostException();
        }
    }

    private boolean alreadyLike(String userKey, Long postId) {
        Double score = redisTemplate.opsForZSet().score(userKey, postId);
        return score != null;
    }

    private double getCurrentTimeInSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
