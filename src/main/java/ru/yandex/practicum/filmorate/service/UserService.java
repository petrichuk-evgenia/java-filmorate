package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final UserDataLoader userDataLoader;

    public UserService(
            @Qualifier("userDbStorage") UserStorage userStorage,
            UserDataLoader userDataLoader) {
        this.userStorage = userStorage;
        this.userDataLoader = userDataLoader;
    }

    public List<User> getAllUsers() {
        log.debug("Запрос на получение всех пользователей");
        List<User> users = userStorage.getAllUsers();
        return enrichUsersWithFriends(users);
    }

    public User getUserById(Long id) {
        log.debug("Запрос на получение пользователя с ID: {}", id);
        User user = userStorage.getUserById(id)
                .orElseThrow(() -> new IdNotFoundException("Пользователь с ID " + id + " не найден"));

        return enrichUserWithFriends(user);
    }

    @Transactional
    public User createUser(@Valid User user) {
        log.info("Создание нового пользователя: {}", user.getLogin());
        validateUser(user);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя '{}' установлено имя из логина", user.getLogin());
        }

        User createdUser = userStorage.createUser(user);
        log.info("Пользователь создан с ID: {}", createdUser.getId());
        return createdUser;
    }

    @Transactional
    public User updateUser(@Valid User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());
        validateUser(user);

        if (user.getId() == null) {
            log.error("Попытка обновить пользователя без ID");
            throw new CustomValidationExpression("ID пользователя должен быть указан");
        }

        if (!userStorage.existsById(user.getId())) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new IdNotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Для пользователя с ID {} установлено имя из логина", user.getId());
        }

        User updatedUser = userStorage.updateUser(user);
        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return updatedUser;
    }

    @Transactional
    public void addFriend(Long userId, Long friendId) {
        log.info("Пользователь {} добавляет в друзья пользователя {}", userId, friendId);

        validateFriendship(userId, friendId);

        userStorage.addFriend(userId, friendId);
        log.info("Пользователь {} успешно добавил в друзья пользователя {}", userId, friendId);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        log.info("Пользователь {} удаляет из друзей пользователя {}", userId, friendId);

        if (!userStorage.existsById(userId)) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new IdNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!userId.equals(friendId) && !userStorage.existsById(friendId)) {
            log.error("Пользователь с ID {} не найден", friendId);
            throw new IdNotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            log.warn("Пользователь {} пытается удалить самого себя из друзей", userId);
            return;
        }

        userStorage.removeFriend(userId, friendId);
        log.info("Операция удаления дружбы между {} и {} завершена", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.debug("Запрос на получение списка друзей пользователя с ID: {}", userId);

        if (!userStorage.existsById(userId)) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new IdNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        List<User> friends = userStorage.getFriends(userId);
        return enrichUsersWithFriends(friends);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.debug("Запрос на получение общих друзей пользователей {} и {}", userId, otherId);

        validateBothUsersExist(userId, otherId);

        List<User> commonFriends = userStorage.getCommonFriends(userId, otherId);
        return enrichUsersWithFriends(commonFriends);
    }

    private List<User> enrichUsersWithFriends(List<User> users) {
        if (users.isEmpty()) {
            return users;
        }

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Long>> userFriendsMap = userDataLoader.loadFriendsForUsers(userIds);

        for (User user : users) {
            user.setFriends(userFriendsMap.getOrDefault(user.getId(), new HashSet<>()));
        }

        return users;
    }

    private User enrichUserWithFriends(User user) {
        Set<Long> friendIds = userDataLoader.loadFriendsForUser(user.getId());
        user.setFriends(friendIds);
        return user;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Попытка создать пользователя с пустым email");
            throw new CustomValidationExpression("Электронная почта не может быть пустой");
        }

        if (!user.getEmail().contains("@")) {
            log.warn("Попытка создать пользователя с некорректным email: {}", user.getEmail());
            throw new CustomValidationExpression("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Попытка создать пользователя с пустым логином");
            throw new CustomValidationExpression("Логин не может быть пустым");
        }

        if (user.getLogin().contains(" ")) {
            log.warn("Попытка создать пользователя с логином, содержащим пробелы: {}", user.getLogin());
            throw new CustomValidationExpression("Логин не может содержать пробелы");
        }

        if (user.getBirthday() == null) {
            log.warn("Попытка создать пользователя без даты рождения");
            throw new CustomValidationExpression("Дата рождения должна быть указана");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Попытка создать пользователя с датой рождения в будущем: {}", user.getBirthday());
            throw new CustomValidationExpression("Дата рождения не может быть в будущем");
        }
    }

    private void validateFriendship(Long userId, Long friendId) {
        validateBothUsersExist(userId, friendId);

        if (userId.equals(friendId)) {
            log.error("Пользователь {} пытается добавить в друзья самого себя", userId);
            throw new CustomValidationExpression("Пользователь не может добавить в друзья самого себя");
        }
    }

    private void validateBothUsersExist(Long userId, Long otherId) {
        if (!userStorage.existsById(userId)) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new IdNotFoundException("Пользователь с ID " + userId + " не найден");
        }

        if (!userStorage.existsById(otherId)) {
            log.error("Пользователь с ID {} не найден", otherId);
            throw new IdNotFoundException("Пользователь с ID " + otherId + " не найден");
        }
    }
}