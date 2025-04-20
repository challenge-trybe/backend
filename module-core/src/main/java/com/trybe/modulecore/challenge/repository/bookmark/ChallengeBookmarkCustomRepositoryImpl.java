package com.trybe.modulecore.challenge.repository.bookmark;

import com.trybe.modulecore.challenge.entity.ChallengeBookmark;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class ChallengeBookmarkCustomRepositoryImpl implements ChallengeBookmarkCustomRepository {
    private final JdbcTemplate jdbcTemplate;

    public ChallengeBookmarkCustomRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void bulkInsert(List<ChallengeBookmark> bookmarks) {
        String sql = "INSERT IGNORE INTO challenge_bookmarks (challenge_id, user_id, created_at) VALUES (?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChallengeBookmark bookmark = bookmarks.get(i);
                ps.setLong(1, bookmark.getChallengeId());
                ps.setLong(2, bookmark.getUserId());
                ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            }

            @Override
            public int getBatchSize() {
                return bookmarks.size();
            }
        });
    }

    @Override
    public void bulkDelete(List<ChallengeBookmark> bookmarks) {
        String sql = "DELETE FROM challenge_bookmarks WHERE challenge_id = ? AND user_id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChallengeBookmark bookmark = bookmarks.get(i);
                ps.setLong(1, bookmark.getChallengeId());
                ps.setLong(2, bookmark.getUserId());
            }

            @Override
            public int getBatchSize() {
                return bookmarks.size();
            }
        });
    }
}
