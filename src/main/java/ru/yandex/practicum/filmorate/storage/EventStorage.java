package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

public interface EventStorage {
    void addEvent(Event event);

    List<Event> getUserFeed(Long userId);

    default void addFriendEvent(Long userId, Long friendId, Operation operation) {
        addEvent(Event.builder()
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .eventType(EventType.FRIEND)
                .operation(operation)
                .entityId(friendId)
                .build());
    }

    default void addLikeEvent(Long userId, Long filmId, Operation operation) {
        addEvent(Event.builder()
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .eventType(EventType.LIKE)
                .operation(operation)
                .entityId(filmId)
                .build());
    }

    default void addReviewEvent(Long userId, Long reviewId, Operation operation) {
        addEvent(Event.builder()
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .eventType(EventType.REVIEW)
                .operation(operation)
                .entityId(reviewId)
                .build());
    }
}
