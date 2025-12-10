package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.errorresp.ValidationErrorResponse;
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
    public void addValidUserTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .name("Eva_name")
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        User addedUser = JsonUtils.convertFromJson(resp.body(), User.class);
        Assert.assertEquals(resp.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(userToAdd.equals(addedUser), "Пользователь добавлен некорректно");
    }

    @Test
    public void addValidUserTestEmptyName() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("Eva")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        User addedUser = JsonUtils.convertFromJson(resp.body(), User.class);
        Assert.assertEquals(resp.statusCode(), 201, "Статус POST /users должен быть 201");
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()), "Пользователь добавлен некорректно");
    }

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
        Assert.assertTrue(addedUser.equals(userToAdd.toBuilder().name(userToAdd.getLogin()).build()), "Пользователь добавлен некорректно");

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

    @Test
    public void addInValidUserTest() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("1?п@")
                .login("Eva ")
                .birthday(LocalDate.of(2222, 4, 26))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Assert.assertEquals(resp.statusCode(), 400, "Статус POST /users должен быть 400");
        ValidationErrorResponse response = JsonUtils.convertFromJson(resp.body(), ValidationErrorResponse.class);
        Assert.assertTrue(response.getError().equals("Ошибка валидации"), "Валидация прошла некорректно");
        Assert.assertTrue(response.getDetails().contains("Логин не может быть null, пустым или содержать пробелы"));
        Assert.assertTrue(response.getDetails().contains("Email должен быть в формате name@example.com"));
        Assert.assertTrue(response.getDetails().contains("День рождения не может быть в будущем"));
    }

    @Test
    public void addInValidUserTest2() throws IOException, InterruptedException {
        User userToAdd = User.builder()
                .email("eva@gmail.com")
                .login("")
                .birthday(LocalDate.of(1987, 4, 26))
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Assert.assertEquals(resp.statusCode(), 400, "Статус POST /users должен быть 400");
        ValidationErrorResponse response = JsonUtils.convertFromJson(resp.body(), ValidationErrorResponse.class);
        Assert.assertTrue(response.getError().equals("Ошибка валидации"), "Валидация прошла некорректно");
        Assert.assertTrue(response.getDetails().contains("Логин не может быть null, пустым или содержать пробелы"));
    }

    @Test
    public void addInValidUserTest3() throws IOException, InterruptedException {
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

        client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(userToAdd.toBuilder().login("Eva2").build())))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Assert.assertEquals(resp.statusCode(), 400, "Статус POST /users должен быть 400");
        ValidationErrorResponse response = JsonUtils.convertFromJson(resp.body(), ValidationErrorResponse.class);
        Assert.assertTrue(response.getError().equals("Ошибка валидации"), "Валидация прошла некорректно");
        Assert.assertTrue(response.getDetails().contains("Email должен быть уникальным"));
    }
}
