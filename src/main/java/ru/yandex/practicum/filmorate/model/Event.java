package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private Long eventId;      // primary key
    private Long userId;       // пользак, совершивший действие
    private Long timestamp;    // время события
    private EventType eventType; // LIKE, REVIEW, FRIEND
    private Operation operation; // REMOVE, ADD, UPDATE
    private Long entityId;     // ID сущности
}
