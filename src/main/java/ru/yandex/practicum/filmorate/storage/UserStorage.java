package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {

    User addUser(User user);

    User updateUser(int id, User user);

    List<User> getAllUsers();

    List<User> clearUsers();

    Map<Integer, User> getUsers();

    User getUser(int id);
}
