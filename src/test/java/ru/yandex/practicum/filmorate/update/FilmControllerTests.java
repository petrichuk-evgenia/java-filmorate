package ru.yandex.practicum.filmorate.update;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.BaseTest;
import ru.yandex.practicum.filmorate.model.Film;
import utils.JsonUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class FilmControllerTests extends BaseTest {

    @Test
    public void updateValidFilmTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("1")
                .description("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToAdd)))
                .build();

        HttpResponse<String> resp1 = client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Film addedFilm = JsonUtils.convertFromJson(resp1.body(), Film.class);
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /films должен быть 201");
        Assert.assertTrue(filmToAdd.equals(addedFilm), "Фильм добавлен некорректно");

        Film filmToUpdate = addedFilm.toBuilder().name("11")
                .description("111111111111111111111111111111111" +
                        "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "111111111111111111111111111111111111111111111111111111111111111111111111111")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1).build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films/" + filmToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Film updatedFilm = JsonUtils.convertFromJson(resp.body(), Film.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /films должен быть 200");
        Assert.assertTrue(filmToUpdate.equals(updatedFilm), "Фильм изменен некорректно");
    }

    @Test
    public void updateInvalidFilmTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("1")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .duration(1)
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToAdd)))
                .build();
        Film addedFilm = JsonUtils.convertFromJson(client.send(req1, HttpResponse.BodyHandlers
                .ofString(StandardCharsets.UTF_8)).body(), Film.class);
        Assert.assertTrue(filmToAdd.equals(addedFilm), "Фильм добавлен некорректно");

        Film filmToUpdate = addedFilm.toBuilder()
                .name("")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(0)
                .description("111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "111111111111111111111111111111111111")
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films/" + filmToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /films должен быть 400");
        Map<String, String> response = JsonUtils.convertFromJson(resp.body(), Map.class);
        Assert.assertTrue(response.get("description").equals("Описание фильма не должно быть больше 200 символов"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("duration").equals("Продолжительность фильма должна быть положительной"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("name").equals("Название фильма не может быть null или пустым"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("releaseDate").equals("Дата релиза должна быть не раньше 28 декабря 1895 года"), "Валидация прошла некорректно");
    }
}
