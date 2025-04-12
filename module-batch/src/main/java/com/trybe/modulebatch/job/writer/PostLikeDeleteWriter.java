package com.trybe.modulebatch.job.writer;

import com.trybe.modulecore.post.entity.PostLike;
import com.trybe.modulecore.post.repository.PostLikeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PostLikeDeleteWriter extends PostLikeWriter {
    private final String POST_LIKE_DELETE_KEY = "post:%d:liked:deleted";
    private final PostLikeRepository postLikeRepository;

    public PostLikeDeleteWriter(RedisTemplate<String, Long> redisTemplate, PostLikeRepository postLikeRepository) {
        super(redisTemplate);
        this.postLikeRepository = postLikeRepository;
    }

    @Override
    protected void dataBaseExecution(List<PostLike> postLikes) {
        postLikeRepository.bulkDelete(postLikes);
    }

    @Override
    protected String getPostRedisKey() {
        return POST_LIKE_DELETE_KEY;
    }
}
