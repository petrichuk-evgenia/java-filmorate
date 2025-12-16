package ru.yandex.practicum.filmorate.get;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.BaseTest;
import ru.yandex.practicum.filmorate.model.User;
import utils.JsonUtils;
import utils.RestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserControllerTests extends BaseTest {

    @Test
    public void getAllUsersNotEmptyTest() throws JsonProcessingException {
        User userToAdd = User.builder()
                .name("Eva")
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        RestUtils.post(getUrl("/users"), userToAdd, headers);
        Response resp = RestUtils.get(getUrl("/users"), ContentType.JSON.toString());
        List<User> users = resp.as(List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /users должен быть 200");
        Assert.assertEquals(users.size(), 1, "Некорректный размер списка пользователей");
        userToAdd.setId(1);
        //Сравним объекты: раз так триггерит наличие конструктора с аннотацией @JsonCreator, то будем делать отстой
        Map<String, String> userToAddMap = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(userToAdd), Map.class);
        Map<String, String> addedUserMap = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(users.get(0)), Map.class);
        Assert.assertTrue(userToAddMap.equals(addedUserMap), "Список пользователей возвращен некорректно");
    }

    @Test
    public void getAllUsersEmptyTest() throws IOException, InterruptedException {
        Response resp = RestUtils.get(getUrl("/users"), ContentType.JSON.toString());
        List<User> users = resp.as(List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /users должен быть 200");
        Assert.assertEquals(users.size(), 0, "Некорректный размер списка пользователей");
    }

    @Test
    public void getUserByIdValid() {
        User userToAdd = User.builder()
                .name("Eva")
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        User addedUser = RestUtils.post(getUrl("/users"), userToAdd, headers).as(User.class);
        Response resp = RestUtils.get(getUrl("/users/" + addedUser.getId()), ContentType.JSON.toString());
        User gottenUser = resp.as(User.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /users/" + addedUser.getId() + " должен быть 200");
        Assert.assertTrue(addedUser.equals(gottenUser), "Пользователь получен некорректно");
    }

    @Test
    public void getUserByIdNotFoundId() {
        Response resp = RestUtils.get(getUrl("/users/1"), ContentType.JSON.toString());
        Assert.assertEquals(resp.statusCode(), 404, "Статус GET /users/1 должен быть 404");
        Map<String, String> errors = resp.as(Map.class);
        Assert.assertTrue(errors.get("error").equals("Пользователь с id=1 не найден"));
    }

    @Test
    public void getUserByIdInvalidId() {
        Response resp = RestUtils.get(getUrl("/users/a"), ContentType.JSON.toString());
        Assert.assertEquals(resp.statusCode(), 500, "Статус GET /users/a должен быть 500");
        Map<String, String> errors = resp.as(Map.class);
        Assert.assertTrue(errors.get("error").contains("Произошла непредвиденная ошибка"));
    }
}
