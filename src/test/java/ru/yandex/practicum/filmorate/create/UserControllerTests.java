package ru.yandex.practicum.filmorate.create;

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
    public void addValidUserTest() {
        User userToAdd = User.builder()
                .name("Eva_name")
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(userToAdd.equals(addedUser), "Пользователь добавлен некорректно");
    }

    @Test
    public void addValidUserTestEmptyName() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        Response resp = RestUtils.post(getUrl("/users"), userToAdd, headers);
        User addedUser = resp.as(User.class);
        userToAdd.setId(addedUser.getId());
        Assert.assertEquals(resp.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd),
                "Пользователь добавлен некорректно");
    }

    @Test
    public void addInValidUserTest() {
        User userToAdd = User.builder()
                .email("1?п@")
                .login("Eva ")
                .birthday(LocalDate.of(2222, 4, 26))
                .build();

        Response resp = RestUtils.post(getUrl("/users"), userToAdd, headers);
        Assert.assertEquals(resp.statusCode(), 400, "Статус POST /users должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("email").equals("Email должен быть в формате name@example.com"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("birthday").equals("День рождения не может быть в будущем"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("login").equals("Логин не может содержать пробелы"), "Валидация прошла некорректно");
    }

    @Test
    public void addInValidUserTest2() {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();


        RestUtils.post(getUrl("/users"), userToAdd, headers);
        Response resp = RestUtils.post(getUrl("/users"), userToAdd.toBuilder().login("Eva2").build(), headers);

        Assert.assertEquals(resp.statusCode(), 400, "Статус POST /users должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("error").equals("Email должен быть уникальным"), "Валидация прошла некорректно");
    }
}
