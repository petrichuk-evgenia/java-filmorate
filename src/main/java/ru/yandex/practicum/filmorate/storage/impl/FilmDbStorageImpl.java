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
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("filmDbStorage")
@Primary
@Repository
public class FilmDbStorageImpl implements FilmStorage {
    private static final Long DEFAULT_MPA_ID = 1L;
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "INNER JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                    "INNER JOIN film_director fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.release_date";

    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name " +
                    "FROM films f " +
                    "INNER JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                    "INNER JOIN film_director fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN (SELECT film_id, COUNT(*) AS likes_count FROM likes GROUP BY film_id) l ON f.film_id = l.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY COALESCE(l.likes_count, 0) DESC";
    private static final String FIND_FILM_BASE_QUERY =
            "SELECT DISTINCT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, " +
                    "COALESCE(l.likes_count, 0) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                    "LEFT JOIN (" +
                    "SELECT film_id, COUNT(*) AS likes_count " +
                    "FROM likes " +
                    "GROUP BY film_id) l ON f.film_id = l.film_id ";
    private static final String FIND_FILM_BY_NAME = "WHERE LOWER(f.name) LIKE ? ORDER BY likes_count DESC";
    private static final String FIND_FILM_BY_DIRECTOR =
            "INNER JOIN film_director fd ON f.film_id = fd.film_id " +
                    "INNER JOIN director d ON fd.director_id = d.director_id " +
                    "WHERE LOWER(d.name) LIKE ? " +
                    "ORDER BY likes_count DESC";
    private static final String FIND_FILM_BY_DIRECTOR_AND_NAME =
            "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN director d ON fd.director_id = d.director_id " +
                    "WHERE LOWER(f.name) LIKE ? OR LOWER(d.name) LIKE ? " +
                    "ORDER BY likes_count DESC";
    private final JdbcTemplate jdbcTemplate;
    private final DirectorDbStorageImpl directorDbStorage;

    public FilmDbStorageImpl(JdbcTemplate jdbcTemplate, DirectorDbStorageImpl directorDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorDbStorage = directorDbStorage;
    }

    /*@Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";

        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }*/

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.name AS mpa_name, " +
                "d.director_id, d.name AS director_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                "LEFT JOIN director d ON fd.director_id = d.director_id " +
                "ORDER BY f.film_id";

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Film> filmsMap = new LinkedHashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Film film = filmsMap.get(filmId);
                if (film == null) {
                    film = mapRowToFilm(rs, 0);
                    filmsMap.put(filmId, film);
                }

                Long directorId = rs.getObject("director_id", Long.class);
                if (directorId != null) {
                    Director director = Director.builder()
                            .id(directorId)
                            .name(rs.getString("director_name"))
                            .build();
                    film.getDirectors().add(director);
                }
            }
            return new ArrayList<>(filmsMap.values());
        });
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
        updateFilmGenres(film.getId(), film.getGenres());
        updateFilmDirectors(film.getId(), film.getDirectors());

        log.info("Фильм создан с ID: {}", filmId);
        return enrichFilm(film);
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
        updateFilmDirectors(film.getId(), film.getDirectors());
        log.info("Фильм с ID {} обновлен", film.getId());
        return enrichFilm(film);
    }

    @Override
    public Optional<Film> getFilmById(Long id) {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";

        try {
            Film film = jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
            if (film != null) {
                enrichFilm(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /*@Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) " +
                "SELECT ?, ? " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM likes WHERE film_id = ? AND user_id = ?" +
                ")";

        int updated = jdbcTemplate.update(sql, filmId, userId, filmId, userId);

        if (updated > 0) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            log.debug("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
        }
    }*/

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) " +
                "SELECT ?, ? " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM likes WHERE film_id = ? AND user_id = ?" +
                ")";

        int updated = jdbcTemplate.update(sql, filmId, userId, filmId, userId);

        if (updated > 0) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            log.debug("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
        }
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

        if (jdbcTemplate.update(sql, filmId, userId) > 0) {
            log.info("Пользователь {} удалил лайк фильму {}", userId, filmId);
        }
    }

    /*@Override
    public List<Film> getPopularFilms(int count, int genreId, int year) {
        String sql = "SELECT f.*, " +
                "m.name AS mpa_name, " +
                "COUNT(l.user_id) AS likes_count, " +
                "g.genre_id, " +
                "g.name AS genre_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE EXTRACT(YEAR FROM f.release_date) = ? " +
                "  AND (? = 0 OR g.genre_id = ?) " +  // 0 означает "все жанры"
                "GROUP BY f.film_id, m.name, g.genre_id, g.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, this::mapRowToFilm, year, genreId, genreId, count);
    }*/

    @Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                        "m.mpa_id, m.name AS mpa_name, COALESCE(COUNT(l.user_id), 0) AS likes_count "
        );

        sql.append("FROM films f ")
                .append("JOIN mpa_ratings m ON f.mpa_id = m.mpa_id ")
                .append("LEFT JOIN likes l ON f.film_id = l.film_id ")
                .append("INNER JOIN film_genres fg ON f.film_id = fg.film_id ")
                .append("INNER JOIN genres g ON fg.genre_id = g.genre_id ")
                .append("WHERE 1=1 ");

        if (genreId != null) {
            sql.append("AND g.genre_id = ? ");
        }
        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.release_date) = ? ");
        }

        sql.append("GROUP BY f.film_id, m.mpa_id, m.name ")
                .append("ORDER BY likes_count DESC, f.film_id ")
                .append("LIMIT ?");

        List<Object> params = new ArrayList<>();
        if (genreId != null) params.add(genreId);
        if (year != null) params.add(year);
        params.add(count);

        return jdbcTemplate.query(sql.toString(), this::mapRowToFilm, params.toArray());
    }

    /*@Override
    public List<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append("    f.film_id, ");
        sql.append("    f.name, ");
        sql.append("    f.description, ");
        sql.append("    f.release_date, ");
        sql.append("    f.duration, ");
        sql.append("    m.mpa_id, ");
        sql.append("    m.name AS mpa_name, ");
        sql.append("    COALESCE(lm.likes_count, 0) AS likes_count, ");
        sql.append("    GROUP_CONCAT(d.director_id) AS director_ids, ");
        sql.append("    GROUP_CONCAT(d.name) AS director_names ");
        sql.append("FROM films f ");
        sql.append("JOIN mpa_ratings m ON f.mpa_id = m.mpa_id ");
        sql.append("LEFT JOIN ( ");
        sql.append("    SELECT film_id, COUNT(user_id) AS likes_count ");
        sql.append("    FROM likes ");
        sql.append("    GROUP BY film_id ");
        sql.append(") lm ON f.film_id = lm.film_id ");
        sql.append("INNER JOIN film_genres fg ON f.film_id = fg.film_id ");
        sql.append("INNER JOIN genres g ON fg.genre_id = g.genre_id ");
        sql.append("LEFT JOIN film_director fd ON f.film_id = fd.film_id ");
        sql.append("LEFT JOIN director d ON fd.director_id = d.director_id ");
        sql.append("WHERE 1=1 ");

        if (genreId != null) {
            sql.append("AND g.genre_id = ? ");
        }
        if (year != null) {
            sql.append("AND EXTRACT(YEAR FROM f.release_date) = ? ");
        }

        sql.append("GROUP BY ");
        sql.append("    f.film_id, f.name, f.description, f.release_date, f.duration, ");
        sql.append("    m.mpa_id, m.name, lm.likes_count ");
        sql.append("ORDER BY likes_count DESC, f.film_id ");
        sql.append("LIMIT ?");

        List<Object> params = new ArrayList<>();
        if (genreId != null) params.add(genreId);
        if (year != null) params.add(year);
        params.add(count);

        return jdbcTemplate.query(sql.toString(), this::mapRowToFilm, params.toArray());
    }*/

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        boolean exists = count != null && count > 0;
        return exists;
    }

    @Override
    public Film deleteFilm(Long id) {
        Optional<Film> film = getFilmById(id);
        if (!film.isPresent()) {
            throw new IdNotFoundException("Фильм с ID " + id + " не найден");
        }
        jdbcTemplate.update(DELETE_QUERY, id);
        return film.get();
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

    @Override
    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        String sql = "year".equals(sortBy) ? GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR :
                "likes".equals(sortBy) ? GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES :
                        null;

        if (sql == null) {
            throw new CustomValidationExpression("Некорректный параметр сортировки: sortBy=" + sortBy);
        }

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, directorId);

        return films.stream()
                .map(this::enrichFilm)
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        List<Object> params = new ArrayList<>();
        String sql = FIND_FILM_BASE_QUERY;

        if ("title,director".equals(by) || "director,title".equals(by)) {
            sql += FIND_FILM_BY_DIRECTOR_AND_NAME;
            params.add("%" + query.toLowerCase() + "%");
            params.add("%" + query.toLowerCase() + "%");
        } else if ("title".equals(by)) {
            sql += FIND_FILM_BY_NAME;
            params.add("%" + query.toLowerCase() + "%");
        } else if ("director".equals(by)) {
            sql += FIND_FILM_BY_DIRECTOR;
            params.add("%" + query.toLowerCase() + "%");
        } else {
            throw new IdNotFoundException("Некорректный параметр поиска: " + by);
        }
        return jdbcTemplate.query(sql, this::mapRowToFilm, params.toArray())
                .stream()
                .map(this::enrichFilm)
                .collect(Collectors.toList());

    }

    private void updateFilmGenres(Long filmId, Set<Genre> genres) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);

        if (genres != null && !genres.isEmpty()) {
            saveFilmGenres(filmId, genres);
        }
    }

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        String sql = "INSERT INTO film_director (film_id, director_id) VALUES (?, ?)";
        for (Director director : directors) {
            jdbcTemplate.update(sql, filmId, director.getId());
        }
    }

    private void updateFilmDirectors(Long filmId, Set<Director> directors) {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", filmId);
        if (directors != null && !directors.isEmpty()) {
            saveFilmDirectors(filmId, directors);
        }
    }

    public List<Genre> getGenresForFilm(Long filmId) {
        String sql = "SELECT g.genre_id AS id, g.name " +
                "FROM genres g " +
                "INNER JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> Genre.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .build(), filmId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
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

    @Override
    public Set<Long> getFilmLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
    }

    @Override
    public Set<Long> getUserLikes(Long userId) {
        String sql = "SELECT film_id FROM likes WHERE user_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, userId));
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        log.debug("Получение рекомендаций для пользователя с ID: {}", userId);

        Set<Long> userLikes = getUserLikes(userId);

        if (userLikes.isEmpty()) {
            log.debug("У пользователя {} нет лайков, возвращаем пустой список рекомендаций", userId);
            return Collections.emptyList(); // ВАЖНО: возвращаем пустой список, а не null
        }

        String similarUsersSql = String.format(
                "SELECT DISTINCT user_id FROM likes WHERE film_id IN (%s) AND user_id != ?",
                userLikes.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );

        List<Long> similarUserIds = jdbcTemplate.queryForList(similarUsersSql, Long.class, userId);

        if (similarUserIds.isEmpty()) {
            log.debug("Не найдено пользователей с похожими вкусами для пользователя {}", userId);
            return Collections.emptyList();
        }

        Map<Long, Integer> commonLikesCount = new HashMap<>();

        String placeholders = similarUserIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String commonLikesSql = String.format(
                "SELECT l2.user_id, COUNT(*) as common_count " +
                        "FROM likes l1 " +
                        "JOIN likes l2 ON l1.film_id = l2.film_id " +
                        "WHERE l1.user_id = ? AND l2.user_id IN (%s) AND l2.user_id != ? " +
                        "GROUP BY l2.user_id",
                placeholders
        );

        List<Object> params = new ArrayList<>();
        params.add(userId);
        params.addAll(similarUserIds);
        params.add(userId);

        try {
            commonLikesCount = jdbcTemplate.query(commonLikesSql, rs -> {
                Map<Long, Integer> map = new HashMap<>();
                while (rs.next()) {
                    map.put(rs.getLong("user_id"), rs.getInt("common_count"));
                }
                return map;
            }, params.toArray());
        } catch (Exception e) {
            log.error("Ошибка при вычислении общих лайков: {}", e.getMessage());
            return Collections.emptyList();
        }

        Long mostSimilarUserId = commonLikesCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostSimilarUserId == null) {
            log.debug("Не удалось определить наиболее похожего пользователя");
            return Collections.emptyList();
        }

        Set<Long> similarUserLikes = getUserLikes(mostSimilarUserId);

        Set<Long> recommendedFilmIds = similarUserLikes.stream()
                .filter(filmId -> !userLikes.contains(filmId))
                .collect(Collectors.toSet());

        if (recommendedFilmIds.isEmpty()) {
            log.debug("У похожего пользователя {} нет новых фильмов для рекомендации", mostSimilarUserId);
            return Collections.emptyList();
        }

        String filmsSql = String.format(
                "SELECT f.*, m.name as mpa_name FROM films f " +
                        "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                        "WHERE f.film_id IN (%s)",
                recommendedFilmIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );

        try {
            List<Film> recommendations = jdbcTemplate.query(filmsSql, this::mapRowToFilm);
            log.info("Для пользователя {} найдено {} рекомендаций", userId, recommendations.size());
            return recommendations;
        } catch (Exception e) {
            log.error("Ошибка при загрузке рекомендованных фильмов: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private Film enrichFilm(Film film) {
        if (film.getId() != null) {
            List<Genre> genres = getGenresForFilm(film.getId());
            film.setGenres(new HashSet<>(genres));

            List<Director> directors = getDirectorsForFilm(film.getId());
            film.setDirectors(new HashSet<>(directors));
        }
        return film;
    }

    public List<Director> getDirectorsForFilm(Long filmId) {
        String sql = "SELECT d.director_id AS id, d.name " +
                "FROM director d " +
                "INNER JOIN film_director fd ON d.director_id = fd.director_id " +
                "WHERE fd.film_id = ? " +
                "ORDER BY d.director_id";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> Director.builder()
                    .id(rs.getLong("director_id"))
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .build(), filmId);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }
}
