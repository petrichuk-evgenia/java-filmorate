package ru.yandex.practicum.filmorate.get;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.yandex.practicum.filmorate.BaseTest;
import ru.yandex.practicum.filmorate.model.Film;
import utils.JsonUtils;
import utils.RestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
public class FilmControllerTests extends BaseTest {

    @Test
    public void getAllFilmsNotEmptyTest() throws IOException, InterruptedException {
        Film filmToAdd = Film.builder()
                .name("1")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .description("11")
                .duration(1)
                .build();

        RestUtils.post(getUrl("/films"), filmToAdd, headers);
        Response resp = RestUtils.get(baseUrl + "/films", ContentType.JSON.toString());
        List<Film> films = resp.as(List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /films должен быть 200");
        Assert.assertEquals(resp.as(List.class).size(), 1, "Некорректный размер списка фильмов");
        filmToAdd.setId(1);
        //Сравним объекты: раз так триггерит наличие конструктора с аннотацией @JsonCreator, то будем делать отстой
        Map<String, String> filmToAddMap = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(filmToAdd), Map.class);
        Map<String, String> addedFilmMap = JsonUtils.convertFromJson(JsonUtils.getDtoAsJsonString(films.get(0)), Map.class);
        Assert.assertTrue(filmToAddMap.equals(addedFilmMap), "Список фильмов возвращен некорректно");
    }

    @Test
    public void getAllFilmsEmptyTest() throws IOException, InterruptedException {
        Response resp = RestUtils.get(baseUrl + "/films", ContentType.JSON.toString());
        List<Film> films = resp.as(List.class);
        Assert.assertEquals(resp.statusCode(), 200, "Статус GET /films должен быть 200");
        Assert.assertEquals(films.size(), 0, "Некорректный размер списка фильмов");
    }
}
