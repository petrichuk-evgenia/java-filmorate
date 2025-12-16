package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@NoArgsConstructor
@Getter
public class InMemoryUserStorage implements UserStorage {

    public static int nextId = 0;
    private final HashMap<Integer, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {
        nextId++;
        user.setId(nextId);
        users.put(user.getId(), validateUser(user));
        log.info("Добавлен пользователь {}", users.get(user.getId()));
        return users.get(user.getId());
    }

    @Override
    public User updateUser(int id, User user) {
        User updatedUser = validateUser(user);
        updatedUser.setId(id);
        users.put(id, updatedUser);
        log.info("Изменен пользователь {}", users.get(id));
        return users.get(user.getId());
    }

    private User validateUser(User user) {
        User finalUser = user;
        if (users.values().stream().anyMatch(user1 -> user1.getEmail().equals(finalUser.getEmail()))) {
            throw new CustomValidationExpression("Email должен быть уникальным");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return users.values().stream().toList();
    }

    @Override
    public User getUser(int id) {
        if (!users.containsKey(id)) {
            throw new IdNotFoundException(String.format("Пользователь с id=%d не найден", id));
        } else {
            return users.get(id);
        }
    }

    @Override
    public List<User> clearUsers() {
        users.clear();
        nextId = 0;
        return users.values().stream().toList();
    }
}
