package com.trybe.modulebatch.job.writer;

import com.trybe.modulecore.post.entity.PostLike;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

public abstract class PostLikeWriter implements ItemWriter<PostLike> {
    private final RedisTemplate<String, Long> redisTemplate;

    public PostLikeWriter(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    protected abstract void dataBaseExecution(List<PostLike> postLikes);
    protected abstract String getPostRedisKey();

    @Override
    public void write(Chunk<? extends PostLike> chunk) throws Exception {
        List<PostLike> postLikes = (List<PostLike>) chunk.getItems();
        dataBaseExecution(postLikes);
        List<String> postKeys = postLikes.stream()
                                         .map(postLike -> String.format(getPostRedisKey(), postLike.getPostId()))
                                         .distinct()
                                         .toList();
        redisTemplate.delete(postKeys);
    }
}
