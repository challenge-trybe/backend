package com.trybe.modulebatch.post.job.reader;

import com.trybe.modulebatch.post.config.PostLikeJobConfig;
import com.trybe.modulecore.post.entity.PostLike;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public abstract class PostLikeReader implements ItemReader<PostLike> {
    public final int CHUNK_SIZE = PostLikeJobConfig.CHUNK_SIZE;

    private final RedisTemplate<String, Long> redisTemplate;

    private Cursor<String> cursor;
    private Iterator<Long> currentUserIds;
    private Long currentPostId;

    private boolean isInit = false;

    public PostLikeReader(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    protected abstract String getRedisScanPattern();

    @Override
    public PostLike read() {
        if (!isInit) {
            initCursor();
        }

        if (currentUserIds == null || !currentUserIds.hasNext()) {
            if (cursor.hasNext()) {
                getNextUserIds();
            } else {
                return null;
            }
        }

        Long userId = currentUserIds.next();
        return PostLike.builder()
                       .postId(currentPostId)
                       .userId(userId)
                       .build();
    }

    private void initCursor() {
        ScanOptions scanOptions = ScanOptions.scanOptions()
                                             .match(getRedisScanPattern())
                                             .count(CHUNK_SIZE)
                                             .build();
        cursor = redisTemplate.scan(scanOptions);
        isInit = true;
    }

    private void getNextUserIds() {
        String key = cursor.next();
        currentPostId = extractPostId(key);
        Set<Long> userIds = redisTemplate.opsForSet().members(key);
        currentUserIds = userIds != null ? userIds.iterator() : Collections.emptyIterator();
    }

    private Long extractPostId(String productKey) {
        return Long.parseLong(productKey.split(":")[1]);
    }
}
