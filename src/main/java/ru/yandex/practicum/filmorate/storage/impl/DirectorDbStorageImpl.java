package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorageImpl implements DirectorStorage {
    private static final String FIND_LAST_ID_QUERY = "SELECT MAX(director_id) FROM director";
    private static final String FIND_ALL_QUERY = "SELECT director_id AS id, name FROM director ORDER BY director_id";
    private static final String FIND_BY_ID_QUERY = "SELECT director_id AS id, name FROM director WHERE director_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO director (name) VALUES(?)";
    private static final String UPDATE_QUERY = "UPDATE director SET name = ? WHERE director_id = ?";
    private static final String DELETE_QUERY = "DELETE FROM director WHERE director_id = ?";

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Director> directorRowMapper = new RowMapper<>() {
        @Override
        public Director mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Director.builder()
                    .id(rs.getLong("id"))
                    .name(rs.getString("name"))
                    .build();
        }
    };

    @Override
    public List<Director> getDirectors() {
        return jdbcTemplate.query(FIND_ALL_QUERY, directorRowMapper);
    }

    @Override
    public Director getDirector(Long id) {
        try {
            return jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, directorRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new IdNotFoundException("Режиссер с id=" + id + " не найден");
        }
    }

    @Override
    public Director createDirector(Director director) {
        if (director == null || director.getName() == null || director.getName().isBlank()) {
            throw new IdNotFoundException("Некорректные данные режиссёра");
        }

        jdbcTemplate.update(INSERT_QUERY, director.getName());

        Long generatedId = jdbcTemplate.queryForObject(FIND_LAST_ID_QUERY, Long.class);
        director.setId(generatedId);

        log.info("Создан режиссер с id={}", director.getId());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        if (director == null || director.getId() == null) {
            throw new IllegalArgumentException("Некорректные данные для обновления");
        }
        getDirector(director.getId());

        int rows = jdbcTemplate.update(UPDATE_QUERY, director.getName(), director.getId());
        if (rows == 0) {
            throw new IdNotFoundException("Не удалось обновить режиссёра с id=" + director.getId());
        }

        log.info("Обновлён режиссер с id={}", director.getId());
        return director;
    }

    @Override
    public Director deleteDirector(Long id) {
        Director director = getDirector(id);
        int rows = jdbcTemplate.update(DELETE_QUERY, id);
        if (rows == 0) {
            throw new IdNotFoundException("Не удалось удалить режиссёра с id=" + id);
        }
        log.info("Удалён режиссер с id={}", id);
        return director;
    }
}
