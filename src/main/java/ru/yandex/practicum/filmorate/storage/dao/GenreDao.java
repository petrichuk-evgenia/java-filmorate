package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDao {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    public Optional<Genre> getGenreById(Long id) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";

        try {
            Genre genre = jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";
        return jdbcTemplate.query(sql, this::mapRowToGenre, filmId);
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("genre_id"), rs.getString("name"));
    }
}