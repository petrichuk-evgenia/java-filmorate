package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("userDbStorage")
@Primary
public class UserDbStorageImpl implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorageImpl filmDbStorage;
    private static final String GET_COMMON_FILMS_QUERY = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name AS mpa_name, COALESCE(l.likes_count, 0) AS likes_count FROM films f INNER JOIN mpa_ratings m ON f.mpa_id = m.mpa_id LEFT JOIN (SELECT film_id, COUNT(*) AS likes_count FROM likes GROUP BY film_id) l ON f.film_id = l.film_id WHERE f.film_id IN (SELECT film_id FROM likes WHERE user_id = ?) AND f.film_id IN (SELECT film_id FROM likes WHERE user_id = ?) ORDER BY likes_count DESC, f.film_id";

    public UserDbStorageImpl(JdbcTemplate jdbcTemplate, FilmDbStorageImpl filmDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmDbStorage = filmDbStorage;
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        log.debug("Получение всех пользователей из БД");
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    public User createUser(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName() != null ? user.getName() : user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long userId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        user.setId(userId);

        log.info("Пользователь создан с ID: {}", userId);
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?";

        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName() != null ? user.getName() : user.getLogin(),
                user.getBirthday(),
                user.getId());

        if (updated == 0) {
            throw new IdNotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        log.info("Пользователь с ID {} обновлен", user.getId());
        return user;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try {
            User user = jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";

        try {
            jdbcTemplate.update(sql, userId, friendId);
            log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
        } catch (DataAccessException e) {
            throw new IdNotFoundException("Пользователь не найден");
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

        int deleted = jdbcTemplate.update(sql, userId, friendId);
        if (deleted == 0) {
            log.debug("Дружба между пользователями {} и {} не найдена", userId, friendId);
        } else {
            log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";

        log.debug("Получение друзей пользователя с ID: {}", userId);
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id AND f1.user_id = ? " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id AND f2.user_id = ?";

        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public void deleteUser(Long id) {
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long otherId) {
        if (!existsById(userId) || !existsById(otherId)) {
            throw new IdNotFoundException("Пользователь не найден");
        }

        List<Film> films = jdbcTemplate.query(GET_COMMON_FILMS_QUERY, filmDbStorage::mapRowToFilm, userId, otherId);

        return films.stream()
                .map(film -> {
                    List<Genre> genres = filmDbStorage.getGenresForFilm(film.getId());
                    film.setGenres(new HashSet<>(genres));
                    return film;
                })
                .collect(Collectors.toList());
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return User.builder()
                .id(rs.getLong("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday").toLocalDate())
                .build();
    }
}