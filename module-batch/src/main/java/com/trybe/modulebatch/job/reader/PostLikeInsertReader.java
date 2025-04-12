package com.trybe.modulebatch.job.reader;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostLikeInsertReader extends PostLikeReader {
    public PostLikeInsertReader(RedisTemplate<String, Long> redisTemplate) {
        super(redisTemplate);
    }

    private final String REDIS_SCAN_PATTERN = "post:*:liked";
    private final String POST_LIKE_KEY = "post:%d:liked";

    @Override
    protected String getRedisScanPattern() {
        return REDIS_SCAN_PATTERN;
    }

    @Override
    protected String getPostRedisKey() {
        return POST_LIKE_KEY;
    }
}
