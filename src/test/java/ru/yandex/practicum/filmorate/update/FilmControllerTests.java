package ru.yandex.practicum.filmorate.update;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.BaseTest;
import ru.yandex.practicum.filmorate.model.Film;
import utils.RestUtils;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
public class FilmControllerTests extends BaseTest {

    @Test
    public void updateValidFilmTest() {
        Film filmToAdd = Film.builder()
                .name("1")
                .description("1111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "1111111111111111111111111111")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1)
                .build();

        Response resp1 = RestUtils.post(getUrl("/films"), filmToAdd, headers);
        Film addedFilm = resp1.as(Film.class);
        filmToAdd.setId(addedFilm.getId());
        Assert.assertEquals(resp1.statusCode(), 201, "Статус POST /films должен быть 201");
        Assert.assertTrue(filmToAdd.equals(addedFilm), "Фильм добавлен некорректно");

        Film filmToUpdate = addedFilm.toBuilder().name("11")
                .description("111111111111111111111111111111111" +
                        "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111"
                        + "111111111111111111111111111111111111111111111111111111111111111111111111111")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(1).build();

        Response resp = RestUtils.put(getUrl("/films/" + filmToAdd.getId()), filmToUpdate, headers);
        Film updatedFilm = resp.as(Film.class);
        filmToUpdate.setId(updatedFilm.getId());
        Assert.assertEquals(resp.statusCode(), 200, "Статус PUT /films должен быть 200");
        Assert.assertTrue(filmToUpdate.equals(updatedFilm), "Фильм изменен некорректно");
    }

    @Test
    public void updateInvalidFilmTest() {
        Film filmToAdd = Film.builder()
                .name("1")
                .releaseDate(LocalDate.of(2025, 1, 1))
                .duration(1)
                .build();

        Response resp1 = RestUtils.post(getUrl("/films"), filmToAdd, headers);
        Film addedFilm = resp1.as(Film.class);
        filmToAdd.setId(addedFilm.getId());
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

        Response resp = RestUtils.put(getUrl("/films/" + filmToAdd.getId()), filmToUpdate, headers);
        Assert.assertEquals(resp.statusCode(), 400, "Статус PUT /films должен быть 400");
        Map<String, String> response = resp.as(Map.class);
        Assert.assertTrue(response.get("description").equals("Описание фильма не должно быть больше 200 символов"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("duration").equals("Продолжительность фильма должна быть положительной"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("name").equals("Название фильма не может быть null или пустым"), "Валидация прошла некорректно");
        Assert.assertTrue(response.get("releaseDate").equals("Дата релиза должна быть не раньше 28 декабря 1895 года"), "Валидация прошла некорректно");
    }
}
