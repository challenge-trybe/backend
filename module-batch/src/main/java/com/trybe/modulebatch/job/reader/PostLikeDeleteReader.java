package com.trybe.modulebatch.job.reader;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostLikeDeleteReader extends PostLikeReader {
    public PostLikeDeleteReader(RedisTemplate<String, Long> redisTemplate) {
        super(redisTemplate);
    }

    private final String REDIS_SCAN_PATTERN = "post:*:liked:users:deleted";

    @Override
    protected String getRedisScanPattern() {
        return REDIS_SCAN_PATTERN;
    }

}
