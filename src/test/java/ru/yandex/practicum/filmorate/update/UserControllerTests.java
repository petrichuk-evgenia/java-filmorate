package ru.yandex.practicum.filmorate.update;

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
import java.util.Map;

@Slf4j
public class UserControllerTests extends BaseTest {

    @Test
    public void updateValidUserTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        User addedUser = JsonUtils.convertFromJson(resp1.body(), User.class);
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .name("Eva_upd")
                .email("eva_upd@gmail.com")
                .login("login")
                .birthday(LocalDate.of(1988, 5, 27))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users/" + userToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        User updatedUser = JsonUtils.convertFromJson(resp.body(), User.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /users должен быть 200");
        Assert.assertTrue(userToUpdate.equals(updatedUser), "Пользователь изменен некорректно");
    }

    @Test
    public void updateValidUserIncorrectEmailTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        User addedUser = JsonUtils.convertFromJson(resp1.body(), User.class);
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("1f?@")
                .login("login")
                .birthday(LocalDate.of(1988, 5, 27))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users/" + userToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = JsonUtils.convertFromJson(resp.body(), Map.class);
        Assert.assertTrue(response.get("email").equals("Email должен быть в формате name@example.com"), "Валидация прошла некорректно");
    }

    @Test
    public void updateValidUserIncorrectLoginTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        User addedUser = JsonUtils.convertFromJson(resp1.body(), User.class);
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("eva@gmail.com")
                .login("login ")
                .birthday(LocalDate.of(1988, 5, 27))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users/" + userToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = JsonUtils.convertFromJson(resp.body(), Map.class);
        Assert.assertTrue(response.get("login").equals("Логин не может содержать пробелы"), "Валидация прошла некорректно");
    }

    @Test
    public void updateValidUserIncorrectBirthdayTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        User addedUser = JsonUtils.convertFromJson(resp1.body(), User.class);
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("eva@gmail.com")
                .login("login")
                .birthday(LocalDate.of(2222, 5, 27))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users/" + userToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /users должен быть 400");
        Map<String, String> response = JsonUtils.convertFromJson(resp.body(), Map.class);
        Assert.assertTrue(response.get("birthday").equals("День рождения не может быть в будущем"), "Валидация прошла некорректно");
    }

    @Test
    public void updateValidUserDuplicateEmailTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();


        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        User addedUser = JsonUtils.convertFromJson(resp1.body(), User.class);
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()),
                "Пользователь добавлен некорректно");

        User userToUpdate = addedUser.toBuilder()
                .email("eva@gmail.com")
                .login("login2")
                .birthday(LocalDate.of(1987, 5, 27))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users/" + userToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assert.assertEquals(resp.statusCode(), 500, "Статус PUT /users должен быть 500");
        Map<String, String> response = JsonUtils.convertFromJson(resp.body(), Map.class);
        Assert.assertTrue(response.get("error").equals("Email должен быть уникальным"), "Валидация прошла некорректно");
    }
}
