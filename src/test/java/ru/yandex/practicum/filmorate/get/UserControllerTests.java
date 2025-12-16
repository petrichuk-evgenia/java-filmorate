package ru.yandex.practicum.filmorate.get;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.BaseTest;
import ru.yandex.practicum.filmorate.model.User;
import utils.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Slf4j
public class UserControllerTests extends BaseTest {

    @Test
    public void getAllUsersNotEmptyTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .name("Eva")
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();
        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();
        client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<User> users = JsonUtils.convertFromJson(resp.body(), List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /users должен быть 200");
        Assert.assertEquals(users.size(), 1, "Некорректный размер списка пользователей");
        User addedUser = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(users.get(0)), User.class);
        Assert.assertTrue(userToAdd.equals(addedUser), "Список пользователей возвращен некорректно");
    }

    @Test
    public void getAllUsersEmptyTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<User> users = JsonUtils.convertFromJson(resp.body(), List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /films должен быть 200");
        Assert.assertEquals(users.size(), 0, "Некорректный размер списка фильмов");
    }
}
