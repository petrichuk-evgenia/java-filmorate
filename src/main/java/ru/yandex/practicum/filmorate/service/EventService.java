package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@Service
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public EventService(EventStorage eventStorage, UserStorage userStorage) {
        this.eventStorage = eventStorage;
        this.userStorage = userStorage;
    }

    public List<Event> getUserFeed(Long userId) {
        log.info("Получение ленты событий для пользователя с ID: {}", userId);

        if (!userStorage.existsById(userId)) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new IdNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        return eventStorage.getUserFeed(userId);
    }
}
