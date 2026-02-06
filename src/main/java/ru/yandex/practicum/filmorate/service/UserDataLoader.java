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
public class UserDataLoader {
    private final JdbcTemplate jdbcTemplate;

    public Map<Long, Set<Long>> loadFriendsForUsers(List<Long> userIds) {
        if (userIds.isEmpty()) {
            log.debug("Список userIds пуст, возвращаем пустую мапу");
            return new HashMap<>();
        }

        String sql = String.format(
                "SELECT user_id, friend_id FROM friendships " +
                        "WHERE user_id IN (%s)",
                userIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","))
        );

        return jdbcTemplate.query(sql, rs -> {
            Map<Long, Set<Long>> result = new HashMap<>();
            while (rs.next()) {
                Long userId = rs.getLong("user_id");
                Long friendId = rs.getLong("friend_id");

                result.computeIfAbsent(userId, k -> new HashSet<>())
                        .add(friendId);
            }
            return result;
        });
    }

    public Set<Long> loadFriendsForUser(Long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Long> friendIds = jdbcTemplate.queryForList(sql, Long.class, userId);
        return new HashSet<>(friendIds);
    }
}