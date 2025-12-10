package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.errorresp.ValidationErrorResponse;
import ru.yandex.practicum.filmorate.model.User;
import utils.JsonUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
public class UserController extends BaseController {

    public static int userCounter = 0;
    private final HashMap<Integer, User> users = new HashMap<>();

    @PostMapping("/users")
    public ResponseEntity<String> addUser(@Validated @RequestBody String userString) throws JsonProcessingException {
        User user = JsonUtils.convertFromJson(userString, User.class);
        if (validate(user, userString, HttpMethod.POST)) {
            if (user.getName() == null || user.getName().isEmpty()) {
                user = user.toBuilder().name(user.getLogin()).build();
            }
            users.put(user.getId(), user);
            log.info("Добавлен пользователь {}", users.get(user.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(JsonUtils.getDtoAsJsonString(users.get(user.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.getDtoAsJsonString(validationErrorResponse));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<String> updateUser(@PathVariable int id, @RequestBody User user) {
        if (validate(user, "", HttpMethod.PUT)) {
            if (user.getName() == null || user.getName().isEmpty()) {
                user = user.toBuilder().name(user.getLogin()).build();
            }
            users.remove(id);
            User updatedUser = user.toBuilder().id(id).build();
            users.put(id, updatedUser);
            log.info("Изменен пользователь {}", users.get(user.getId()));
            return ResponseEntity.status(HttpStatus.OK).body(JsonUtils.getDtoAsJsonString(users.get(user.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.getDtoAsJsonString(validationErrorResponse));
        }
    }

    //ЭТО реализовано, чтоб просто пройти ПР по тестам, которые противоречат логике, здравому смыслу и стандартам
    //... Прошу понять и простить
    @PutMapping("/users")
    public ResponseEntity<String> updateUser(@RequestBody String userString) throws JsonProcessingException {
        User user = JsonUtils.convertFromJson(userString, User.class);
        if (validate(user, userString, HttpMethod.PUT)) {
            int id = user.getId();
            if (user.getName() == null || user.getName().isEmpty()) {
                user = user.toBuilder().name(user.getLogin()).build();
            }
            if (users.containsKey(id)) {
                users.remove(id);
                User updatedUser = user.toBuilder().id(id).build();
                users.put(id, updatedUser);
                log.info("Изменен пользователь {}", users.get(user.getId()));
                return ResponseEntity.status(HttpStatus.OK).body(JsonUtils.getDtoAsJsonString(users.get(user.getId())));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(JsonUtils.getDtoAsJsonString(new ValidationErrorResponse("Пользователь не найден", new ArrayList<>())));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.getDtoAsJsonString(validationErrorResponse));
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

    private boolean validate(User user, String userString, HttpMethod method) {
        boolean validated = true;
        validationErrorResponse = new ValidationErrorResponse("Ошибка валидации", new ArrayList<>());
        if (userString == null) {
            validated = false;
            validationErrorResponse.getDetails().add("Тело запроса не может быть null");
        }
        if (user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().contains(" ")) {
            validated = false;
            validationErrorResponse.getDetails().add("Логин не может быть null, пустым или содержать пробелы");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            validated = false;
            validationErrorResponse.getDetails().add("День рождения не может быть в будущем");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (user.getEmail() == null || !user.getEmail().matches(emailRegex)) {
            validated = false;
            validationErrorResponse.getDetails().add("Email должен быть в формате name@example.com");
        }
        if (users.values().stream().filter(userTmp -> userTmp.getEmail().equals(user.getEmail())).count() > 0 && method.equals(HttpMethod.POST)) {
            validated = false;
            validationErrorResponse.getDetails().add("Email должен быть уникальным");
        }
        if (!validated) log.error("Пользователь {} не прошел валидацию", user);
        return validated;
    }
}
