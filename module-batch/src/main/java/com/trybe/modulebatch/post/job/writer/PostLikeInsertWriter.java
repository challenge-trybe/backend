package com.trybe.modulebatch.post.job.writer;

import com.trybe.modulecore.post.entity.PostLike;
import com.trybe.modulecore.post.repository.PostLikeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostLikeInsertWriter extends PostLikeWriter {
    private final String POST_LIKE_KEY = "post:%d:liked:users";
    private final PostLikeRepository postLikeRepository;

    public PostLikeInsertWriter(RedisTemplate<String, Long> redisTemplate, PostLikeRepository postLikeRepository) {
        super(redisTemplate);
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    protected void dataBaseExecution(List<PostLike> postLikes) {
        postLikeRepository.bulkInsert(postLikes);
    }

    @Override
    protected String getPostRedisKey() {
        return POST_LIKE_KEY;
    }

    @Override
    protected boolean shouldDeleteRedisKey() {
        return false;
    }
}
