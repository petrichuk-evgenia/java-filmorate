package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Component("filmDbStorage")
@Primary
@Repository
public class FilmDbStorageImpl implements FilmStorage {
    private static final Long DEFAULT_MPA_ID = 1L;

    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorageImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";

        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film createFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            Long mpaId = (film.getMpa() != null && film.getMpa().getId() != null)
                    ? film.getMpa().getId()
                    : DEFAULT_MPA_ID;
            ps.setLong(5, mpaId);

            return ps;
        }, keyHolder);

        Long filmId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(filmId);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(filmId, film.getGenres());
        }

        log.info("Фильм создан с ID: {}", filmId);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";

        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new IdNotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        updateFilmGenres(film.getId(), film.getGenres());

        log.info("Фильм с ID {} обновлен", film.getId());
        return film;
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

        if (jdbcTemplate.update(sql, filmId, userId) > 0) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        if (jdbcTemplate.update(sql, filmId, userId) > 0) {
            log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name as mpa_name, COUNT(l.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id, m.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        boolean exists = count != null && count > 0;
        return exists;
    }

    @Override
    public void deleteFilm(Long id) {
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Genre> genreList = new ArrayList<>(genres);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setLong(1, filmId);
                ps.setLong(2, genreList.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return genreList.size();
            }
        });
    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        }
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = null;
        Long mpaId = rs.getLong("mpa_id");
        String mpaName = rs.getString("mpa_name");

        if (mpaId != 0 && mpaName != null) {
            mpa = new Mpa(mpaId, mpaName);
        }

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .mpa(mpa)
                .build();
    }
}
