package ru.yandex.practicum.filmorate.get;

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
import java.util.List;

@Slf4j
public class FilmControllerTests extends BaseTest {

    @Test
    public void getAllFilmsNotEmptyTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("1")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToAdd)))
                .build();

        client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<Film> films = JsonUtils.convertFromJson(resp.body(), List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /films должен быть 200");
        Assert.assertEquals(films.size(), 1, "Некорректный размер списка фильмов");
        Film updatedFilm = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(films.get(0)), Film.class);
        Assert.assertTrue(filmToAdd.equals(updatedFilm), "Список фильмов возвращен некорректно");
    }

    @Test
    public void getAllFilmsEmptyTest() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .GET()
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        List<Film> films = JsonUtils.convertFromJson(resp.body(), List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /films должен быть 200");
        Assert.assertEquals(films.size(), 0, "Некорректный размер списка фильмов");
    }
}
