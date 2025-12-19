package ru.yandex.practicum.filmorate.update;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.BaseTest;
import ru.yandex.practicum.filmorate.model.User;
import utils.RestUtils;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class UserControllerTests extends BaseTest {

    @Test
    public void updateValidUserTest() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp1 = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp1.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .name("Eva_upd")
                .email("eva_upd@gmail.com")
                .login("login")
                .birthday(LocalDate.of(1988, 5, 27))
                .build();

        Response resp = RestUtils.put(getUrl("/users/" + userToAdd.getId()), userToUpdate, headers);
        User updatedUser = resp.as(User.class);
        userToUpdate.setId(updatedUser.getId());
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /users должен быть 200");
        Assert.assertTrue(userToUpdate.equals(updatedUser), "Пользователь изменен некорректно");
    }

    @Test
    public void updateValidUserIncorrectEmailTest() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp1 = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp1.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("1f?@")
                .login("login")
                .birthday(LocalDate.of(1988, 5, 27))
                .build();

        Response resp = RestUtils.put(getUrl("/users/" + userToAdd.getId()), userToUpdate, headers);
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("email").equals("Email должен быть в формате name@example.com"), "Валидация прошла некорректно");
    }

    @Test
    public void updateValidUserIncorrectLoginTest() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp1 = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp1.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("eva@gmail.com")
                .login("login ")
                .birthday(LocalDate.of(1988, 5, 27))
                .build();

        Response resp = RestUtils.put(getUrl("/users/" + userToAdd.getId()), userToUpdate, headers);
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("login").equals("Логин не может содержать пробелы"), "Валидация прошла некорректно");
    }

    @Test
    public void updateValidUserIncorrectBirthdayTest() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp1 = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp1.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("eva@gmail.com")
                .login("login")
                .birthday(LocalDate.of(2222, 5, 27))
                .build();

        Response resp = RestUtils.put(getUrl("/users/" + userToAdd.getId()), userToUpdate, headers);
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("birthday").equals("День рождения не может быть в будущем"), "Валидация прошла некорректно");
    }

    @Test
    public void updateValidUserDuplicateEmailTest() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp1 = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp1.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("eva@gmail.com")
                .login("login2")
                .birthday(LocalDate.of(1987, 5, 27))
                .build();

        Response resp = RestUtils.put(getUrl("/users/" + userToAdd.getId()), userToUpdate, headers);
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("error").equals("Email должен быть уникальным"), "Валидация прошла некорректно");
    }
}
