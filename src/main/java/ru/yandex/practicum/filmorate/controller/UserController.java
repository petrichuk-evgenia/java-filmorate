package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.getUserStorage().addUser(user));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable int id, @Valid @RequestBody User user) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserStorage().updateUser(id, user));
    }

    @GetMapping("users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserStorage().getAllUsers());
    }

    @GetMapping("/users/clear")
    public ResponseEntity<List<User>> clearUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserStorage().clearUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@Valid @PathVariable int id) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserStorage().getUser(id));
    }

    @GetMapping("/users/{id}/friends")
    public ResponseEntity<List<User>> getUserFriends(@Valid @PathVariable int id) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByListIDs(userService.getUserStorage().getUser(id).getFriends().stream().toList()));
    }

    @PutMapping("/users/{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> addToFriends(@Valid @PathVariable int id, @Valid @PathVariable int friendId) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(userService.addToFriends(id, friendId));
    }

    @DeleteMapping("/users/{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> deleteFromFriends(@Valid @PathVariable int id, @Valid @PathVariable int friendId) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(userService.deleteFromFriends(id, friendId));
    }

    @GetMapping("/users/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getUserCommonFriends(@Valid @PathVariable int id, @Valid @PathVariable int otherId) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(userService.getUsersByListIDs(userService.getCommonFriendsList(id, otherId).stream().toList()));
    }

    //ЭТО реализовано, чтоб просто пройти ПР по тестам, которые противоречат логике, здравому смыслу и стандартам
    //... Прошу понять и простить
    @PutMapping("/users")
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        int id = user.getId();
        if (userService.getUserStorage().getUsers().containsKey(id)) {
            userService.getUserStorage().updateUser(id, user);
            log.info("Изменен пользователь {}", userService.getUserStorage().getUsers().get(user.getId()));
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserStorage().getUsers().get(user.getId()));
        } else {
            throw new IdNotFoundException("Пользователь не найден");
        }
    }
}
