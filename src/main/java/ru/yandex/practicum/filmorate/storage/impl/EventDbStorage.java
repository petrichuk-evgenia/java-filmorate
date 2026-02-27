package ru.yandex.practicum.filmorate.storage.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class EventDbStorage implements EventStorage {
    private final JdbcTemplate jdbcTemplate;

    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(Event event) {
        log.debug("Добавление события: userId={}, eventType={}, operation={}, entityId={}, timestamp={}",
                event.getUserId(), event.getEventType(), event.getOperation(), event.getEntityId(), event.getTimestamp());

        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"event_id"});
            ps.setLong(1, event.getUserId());
            ps.setTimestamp(2, new Timestamp(event.getTimestamp()));
            ps.setString(3, event.getEventType().name());
            ps.setString(4, event.getOperation().name());
            ps.setLong(5, event.getEntityId());
            return ps;
        }, keyHolder);

        Long eventId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        event.setEventId(eventId);

        log.info("Событие {} с ID {} успешно добавлено: userId={}, eventType={}, operation={}, entityId={}",
                event.getEventType(), eventId, event.getUserId(), event.getEventType(), event.getOperation(), event.getEntityId());
    }

    @Override
    public List<Event> getUserFeed(Long userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY sequence ASC, event_id ASC";
        log.debug("Получение ленты событий для пользователя с ID: {}", userId);
        return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getLong("event_id"))
                .userId(rs.getLong("user_id"))
                .timestamp(rs.getTimestamp("timestamp").getTime())
                .eventType(EventType.valueOf(rs.getString("event_type")))
                .operation(Operation.valueOf(rs.getString("operation")))
                .entityId(rs.getLong("entity_id"))
                .build();
    }
}