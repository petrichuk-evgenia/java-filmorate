package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilmDataLoader {
    private final JdbcTemplate jdbcTemplate;

    public Map<Long, Set<Long>> loadGenresForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String sql = String.format(
                "SELECT film_id, genre_id FROM film_genres " +
                        "WHERE film_id IN (%s) " +
                        "ORDER BY film_id, genre_id",
                filmIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Set<Long>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Long genreId = rs.getLong("genre_id");

                result.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                        .add(genreId);
            }
            return result;
        });
    }

    public Map<Long, Set<Long>> loadLikesForFilms(List<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return new HashMap<>();
        }

        String sql = String.format(
                "SELECT film_id, user_id FROM likes " +
                        "WHERE film_id IN (%s)",
                filmIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Set<Long>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Long userId = rs.getLong("user_id");

                result.computeIfAbsent(filmId, k -> new HashSet<>())
                        .add(userId);
            }

            return result;
        });
    }

    public Set<Long> loadGenresForFilm(Long filmId) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ? ORDER BY genre_id";
        List<Long> genreIds = jdbcTemplate.queryForList(sql, Long.class, filmId);
        return new LinkedHashSet<>(genreIds);
    }

    public Set<Long> loadLikesForFilm(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> userIds = jdbcTemplate.queryForList(sql, Long.class, filmId);
        return new HashSet<>(userIds);
    }
}
