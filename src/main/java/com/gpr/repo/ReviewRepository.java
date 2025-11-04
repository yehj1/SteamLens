package com.gpr.repo;

import com.gpr.domain.Review;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Review> rowMapper = new RowMapper<>() {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID id = rs.getObject("id", UUID.class);
            UUID appId = rs.getObject("app_id", UUID.class);
            String source = rs.getString("review_source");
            String country = rs.getString("country");
            String lang = rs.getString("lang");
            Integer rating = (Integer) rs.getObject("rating");
            String author = rs.getString("author");
            String content = rs.getString("content");
            Timestamp reviewedAt = rs.getTimestamp("reviewed_at");
            Instant reviewed = reviewedAt != null ? reviewedAt.toInstant() : null;
            String extra = rs.getString("extra");
            String recommendationId = rs.getString("recommendation_id");
            Integer playtimeForever = (Integer) rs.getObject("playtime_forever");
            String authorId = rs.getString("author_id");
            return new Review(id, appId, source, recommendationId, country, lang, rating, author, content, reviewed,
                    extra, recommendationId, playtimeForever, authorId);
        }
    };

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int saveIfNotExists(Review review) {
        return jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reviews(id, app_id, review_source, country, lang, rating, author, content, reviewed_at, extra, recommendation_id, playtime_forever, author_id) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT (id) DO NOTHING");
            ps.setObject(1, review.getId());
            ps.setObject(2, review.getAppId());
            ps.setString(3, review.getReviewSource());
            ps.setString(4, review.getCountry());
            ps.setString(5, review.getLang());
            if (review.getRating() != null) {
                ps.setInt(6, review.getRating());
            } else {
                ps.setObject(6, null);
            }
            ps.setString(7, review.getAuthor());
            ps.setString(8, review.getContent());
            if (review.getReviewedAt() != null) {
                ps.setTimestamp(9, Timestamp.from(review.getReviewedAt()));
            } else {
                ps.setTimestamp(9, null);
            }
            ps.setObject(10, jsonb(review.getExtraJson()));
            ps.setString(11, review.getRecommendationId());
            if (review.getPlaytimeForever() != null) {
                ps.setInt(12, review.getPlaytimeForever());
            } else {
                ps.setObject(12, null);
            }
            ps.setString(13, review.getAuthorId());
            return ps;
        });
    }

    public List<Review> findRecentByApp(UUID appId, List<String> countries, int limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM reviews WHERE app_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(appId);
        if (countries != null && !countries.isEmpty()) {
            sql.append(" AND country = ANY(?)");
            params.add(countries.toArray(new String[0]));
        }
        sql.append(" ORDER BY reviewed_at DESC NULLS LAST, created_at DESC LIMIT ?");
        params.add(limit);
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql.toString());
            ps.setObject(1, params.get(0));
            int idx = 2;
            if (countries != null && !countries.isEmpty()) {
                ps.setArray(idx++, con.createArrayOf("text", countries.toArray()));
            }
            ps.setInt(idx, limit);
            return ps;
        }, rowMapper);
    }

    public boolean existsBySourceAndRecommendationId(String source, String recommendationId) {
        if (recommendationId == null || recommendationId.isBlank()) {
            return false;
        }
        Integer found = jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM reviews WHERE review_source = ? AND recommendation_id = ? LIMIT 1");
            ps.setString(1, source);
            ps.setString(2, recommendationId);
            return ps;
        }, rs -> rs.next() ? 1 : null);
        return found != null;
    }

    public List<Review> findByAppAndCountry(UUID appId, String country, int limit) {
        return jdbcTemplate.query(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM reviews WHERE app_id = ?" +
                            (country != null ? " AND country = ?" : "") +
                            " ORDER BY reviewed_at DESC NULLS LAST, created_at DESC LIMIT ?");
            ps.setObject(1, appId);
            int idx = 2;
            if (country != null) {
                ps.setString(idx++, country);
            }
            ps.setInt(idx, limit);
            return ps;
        }, rowMapper);
    }

    private PGobject jsonb(String json) {
        if (json == null) {
            return null;
        }
        PGobject object = new PGobject();
        object.setType("jsonb");
        try {
            object.setValue(json);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to set jsonb value", e);
        }
        return object;
    }
}
