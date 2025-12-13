package ru.yandex.practicum.filmorate;

import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.error.ValidationErrorResponse;
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
    public void addValidFilmTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("1")
                .description("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToAdd)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        Film addedFilm = JsonUtils.convertFromJson(resp.body(), Film.class);
        Assert.assertEquals(resp.statusCode(), 201, "Статус POST /films должен быть 201");
        Assert.assertTrue(filmToAdd.equals(addedFilm), "Фильм добавлен некорректно");
    }

    @Test
    public void updateValidFilmTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("1")
                .description("description")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .duration(1)
                .build();

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToAdd)))
                .build();
        Film addedFilm = JsonUtils.convertFromJson(client.send(req1,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)).body(), Film.class);
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

    @Test
    public void addInvalidFilmTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("")
                .description("11111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                        "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                        "111111111111111111111111111111")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(0)
                .build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToAdd)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ValidationErrorResponse response = JsonUtils.convertFromJson(resp.body(), ValidationErrorResponse.class);
        Assert.assertEquals(resp.statusCode(), 400, "Статус POST /films должен быть 400");
        Assert.assertTrue(response.getError().equals("Ошибка валидации"), "Валидация прошла некорректно");
        Assert.assertTrue(response.getDetails().contains("Название фильма не может быть null или пустым"));
        Assert.assertTrue(response.getDetails().contains("Продолжительность фильма должна быть положительной"));
        Assert.assertTrue(response.getDetails().contains("Дата релиза должна быть не раньше 28 декабря 1895 года"));
        Assert.assertTrue(response.getDetails().contains("Описание фильма не должно быть больше 200 символов"));
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
                .releaseDate(LocalDate.of(1895, 12, 27)).duration(0).build();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/films/" + filmToAdd.getId()))
                .header("Content-Type", "application/json; charset=UTF-8")
                .PUT(HttpRequest.BodyPublishers.ofString(JsonUtils.getDtoAsJsonString(filmToUpdate)))
                .build();

        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        ValidationErrorResponse response = JsonUtils.convertFromJson(resp.body(), ValidationErrorResponse.class);
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /films должен быть 400");
        Assert.assertTrue(response.getError().equals("Ошибка валидации"), "Валидация прошла некорректно");
        Assert.assertTrue(response.getDetails().contains("Название фильма не может быть null или пустым"));
        Assert.assertTrue(response.getDetails().contains("Продолжительность фильма должна быть положительной"));
        Assert.assertTrue(response.getDetails().contains("Дата релиза должна быть не раньше 28 декабря 1895 года"));
    }
}
