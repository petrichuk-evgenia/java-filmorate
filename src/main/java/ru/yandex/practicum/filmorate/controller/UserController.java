package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
public class UserController {

    public static int userCounter = 0;
    private final HashMap<Integer, User> users = new HashMap<>();

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) throws CustomValidationExpression {
        User finalUser = user;
        if (users.values().stream().anyMatch(user1 -> user1.getEmail().equals(finalUser.getEmail()))) {
            throw new CustomValidationExpression("Email должен быть уникальным");
        } else {
            if (user.getName() == null || user.getName().isEmpty()) {
                user = user.toBuilder().name(user.getLogin()).build();
            }
            users.put(user.getId(), user);
            log.info("Добавлен пользователь {}", users.get(user.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(users.get(user.getId()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @Valid @RequestBody User user) throws CustomValidationExpression {
        User finalUser = user;
        if (users.values().stream().anyMatch(user1 -> user1.getEmail().equals(finalUser.getEmail()))) {
            throw new CustomValidationExpression("Email должен быть уникальным");
        } else {
            if (user.getName() == null || user.getName().isEmpty()) {
                user = user.toBuilder().name(user.getLogin()).build();
            }
            User updatedUser = user.toBuilder().id(id).build();
            users.put(id, updatedUser);
            log.info("Изменен пользователь {}", users.get(user.getId()));
            return ResponseEntity.status(HttpStatus.OK).body(users.get(user.getId()));
        }
    }

    //ЭТО реализовано, чтоб просто пройти ПР по тестам, которые противоречат логике, здравому смыслу и стандартам
    //... Прошу понять и простить
    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        int id = user.getId();
        if (users.containsKey(id)) {
            if (user.getName() == null || user.getName().isEmpty()) {
                user = user.toBuilder().name(user.getLogin()).build();
            }
            User updatedUser = user.toBuilder().id(id).build();
            users.put(id, updatedUser);
            log.info("Изменен пользователь {}", users.get(user.getId()));
            return ResponseEntity.status(HttpStatus.OK).body(users.get(user.getId()));
        } else {
            throw new CustomValidationExpression("Пользователь не найден");
        }
    }

    @GetMapping("users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(users.values().stream().toList());
    }

    @GetMapping("/users/clear")
    public ResponseEntity<List<User>> clearUsers() {
        users.clear();
        return ResponseEntity.status(HttpStatus.OK).body(users.values().stream().toList());
    }
}
