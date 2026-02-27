package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Review> reviewRowMapper = new ReviewRowMapper();

    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review save(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful, created_at) " +
                "VALUES (?, ?, ?, ?, 0, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            return ps;
        }, keyHolder);

        long generatedId = keyHolder.getKey().longValue();
        review.setReviewId(generatedId);
        review.setUseful(0);
        review.setCreatedAt(now);
        return review;
    }

    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, " +
                "is_positive = ? " +
                "WHERE review_id = ?";
        jdbcTemplate.update(sql, review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        return getById(review.getReviewId()).orElseThrow();
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    public Optional<Review> getById(Long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, reviewRowMapper, id);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    public List<Review> getByFilmId(Long filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, reviewRowMapper, filmId, count);
    }

    public List<Review> getAll(int count) {
        String sql = "SELECT * FROM reviews " +
                "ORDER BY useful DESC " +
                "LIMIT ?";
        return jdbcTemplate.query(sql, reviewRowMapper, count);
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void recalculateUseful(Long reviewId) {
        String sql = "UPDATE reviews SET useful = (" +
                "SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0) " +
                "FROM review_feedback WHERE review_id = ?" +
                ") WHERE review_id = ?";
        jdbcTemplate.update(sql, reviewId, reviewId);
    }

    static class ReviewRowMapper implements RowMapper<Review> {
        @Override
        public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Review.builder()
                    .reviewId(rs.getLong("review_id"))
                    .content(rs.getString("content"))
                    .isPositive(rs.getBoolean("is_positive"))
                    .userId(rs.getLong("user_id"))
                    .filmId(rs.getLong("film_id"))
                    .useful(rs.getInt("useful"))
                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                    .build();
        }
    }
}
