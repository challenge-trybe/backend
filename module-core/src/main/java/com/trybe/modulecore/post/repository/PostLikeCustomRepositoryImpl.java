package com.trybe.modulecore.post.repository;

import com.trybe.modulecore.post.entity.PostLike;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PostLikeCustomRepositoryImpl implements PostLikeCustomRepository{
    private final JdbcTemplate jdbcTemplate;

    public PostLikeCustomRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void bulkInsert(List<PostLike> postLikes) {
        String sql = "INSERT IGNORE INTO post_likes (user_id, post_id, created_at) VALUES (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    PostLike like = postLikes.get(i);
                    ps.setLong(1, like.getUserId());
                    ps.setLong(2, like.getPostId());
                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                }

                @Override
                public int getBatchSize() {
                    return postLikes.size();
                }
            });
    }

    @Override
    public void bulkDelete(List<PostLike> postLikes) {
        String sql = "DELETE FROM post_likes WHERE user_id = ? AND post_id = ?";
        List<Object[]> params = new ArrayList<>();
        for (PostLike postLike : postLikes) {
            params.add(new Object[]{postLike.getUserId(), postLike.getPostId()});
        }
        jdbcTemplate.batchUpdate(sql, params);
    }
}
