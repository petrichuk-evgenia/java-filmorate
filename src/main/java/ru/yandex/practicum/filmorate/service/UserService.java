package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Map<String, String> addToFriends(int id, int friendId) {
        validateIds(id, friendId);
        Map<String, String> response = new HashMap<>();
        if (userStorage.getUsers().containsKey(id) && userStorage.getUsers().containsKey(friendId)) {
            Set<Integer> friends1 = userStorage.getUsers().get(id).getFriends();
            Set<Integer> friends2 = userStorage.getUsers().get(friendId).getFriends();
            friends1.add(friendId);
            friends2.add(id);
            if (friends1.contains(friendId) && friends2.contains(id)) {
                response.put("message", String.format("Пользователи %d и %d теперь друзья", id, friendId));
            }
        }
        return response;
    }

    public Map<String, String> deleteFromFriends(int id, int friendId) {
        validateIds(id, friendId);
        Map<String, String> response = new HashMap<>();
        if (userStorage.getUsers().containsKey(id) && userStorage.getUsers().containsKey(friendId)) {
            Set<Integer> friends1 = userStorage.getUsers().get(id).getFriends();
            Set<Integer> friends2 = userStorage.getUsers().get(friendId).getFriends();
            friends1.remove(friendId);
            friends2.remove(id);
            if (!friends1.contains(friendId) && !friends2.contains(id)) {
                response.put("message", String.format("Пользователи %d и %d больше не друзья", id, friendId));
            }
        }
        return response;
    }

    public Set<Integer> getCommonFriendsList(int id, int friendId) {
        validateIds(id, friendId);
        Set<Integer> friends1 = userStorage.getUsers().get(id).getFriends();
        Set<Integer> friends2 = userStorage.getUsers().get(friendId).getFriends();
        return friends1.stream().filter(id1 -> friends2.contains(id1)).collect(Collectors.toSet());
    }

    public List<User> getUsersByListIDs(List<Integer> ids) {
        List<User> userList = new ArrayList<>();
        ids.forEach(id -> {
            userList.add(userStorage.getUsers().get(id));
        });
        return userList;
    }

    private void validateIds(int id, int friendId) {
        /*if (id == 0 || friendId == 0) {
            throw new CustomValidationExpression("Один из идентификаторов равен 0");
        }*/
        if (!userStorage.getUsers().containsKey(id) || !userStorage.getUsers().containsKey(friendId)) {
            throw new IdNotFoundException("Один из пользователей не найден");
        }
        if (id == friendId) {
            throw new CustomValidationExpression("Идентификаторы пользователей совпадают");
        }
    }
}
