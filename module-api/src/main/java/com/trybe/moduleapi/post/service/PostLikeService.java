package com.trybe.moduleapi.post.service;

import com.trybe.moduleapi.common.dto.PageResponse;
import com.trybe.moduleapi.post.dto.PostResponse;
import com.trybe.moduleapi.post.service.event.PostEvent;
import com.trybe.moduleapi.post.service.event.pub.PostEventPublisher;
import com.trybe.moduleapi.post.service.event.PostEventType;
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
    private final String USER_REDIS_PREFIX = "user";
    private final String POST_REDIS_PREFIX = "post";
    private final RedisTemplate<String, Long> redisTemplate;
    private final PostRepository postRepository;
    private final PostEventPublisher eventPublisher;

    public PostLikeService(RedisTemplate<String, Long> redisTemplate, PostRepository postRepository, PostEventPublisher eventPublisher) {
        this.redisTemplate = redisTemplate;
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PostResponse.Like addLike(User user, Long postId) {
        validateExistPost(postId);

        String userKey = createRedisKey(USER_REDIS_PREFIX, user.getId());
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);
        int likeCount = count(postId);

        if (!alreadyLike(userKey, postId)) {
            double score = getCurrentTimeInSeconds();
            redisTemplate.opsForZSet().add(userKey, postId, score);
            redisTemplate.opsForSet().add(postKey, user.getId());
            likeCount++;
        }
        eventPublisher.publish(PostEvent.from(postId, PostEventType.POST_LIKED));
        return PostResponse.Like.from(likeCount, true);
    }

    @Transactional
    public PostResponse.Like removeLike(User user, Long postId){
        validateExistPost(postId);

        String userKey = createRedisKey(USER_REDIS_PREFIX, user.getId());
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);
        int likeCount = count(postId);

        if (alreadyLike(userKey, postId)) {
            redisTemplate.opsForZSet().remove(userKey, postId);
            redisTemplate.opsForSet().remove(postKey, user.getId());
            likeCount--;
        }

        eventPublisher.publish(PostEvent.from(postId, PostEventType.POST_UNLIKED));
        return PostResponse.Like.from(likeCount,false);
    }

    public void removeLikesByPost(Long postId){
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);
        Set<Long> userIds = redisTemplate.opsForSet().members(postKey);
        if (userIds != null) {
            for (Long userId : userIds) {
                String userKey = createRedisKey(USER_REDIS_PREFIX, userId);
                redisTemplate.opsForZSet().remove(userKey, postId);
            }
            redisTemplate.delete(postKey);
        }
    }

    public int count(Long postId){
        String postKey = createRedisKey(POST_REDIS_PREFIX, postId);
        Long count = redisTemplate.opsForSet().size(postKey);
        return count == null ? 0 : count.intValue();
    }

    @Transactional
    public PageResponse<PostResponse.Summary> getLikePostByUser(User user, Pageable pageable){
        String userKey = createRedisKey(USER_REDIS_PREFIX, user.getId());

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

    private String createRedisKey(String domain, Long id){
        return String.format("%s:%d", domain.toLowerCase(), id);
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
