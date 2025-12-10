package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.errorresp.ValidationErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;
import utils.JsonUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@RestController
public class FilmController extends BaseController {

    public static int filmCounter = 0;
    private final HashMap<Integer, Film> films = new HashMap<>();

    @PostMapping("/films")
    public ResponseEntity<String> addFilm(@RequestBody String filmString) throws JsonProcessingException {
        Film film = JsonUtils.convertFromJson(filmString, Film.class);
        if (validate(film, filmString)) {
            films.put(film.getId(), film);
            log.info("Добавлен фильм {}", films.get(film.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(JsonUtils.getDtoAsJsonString(films.get(film.getId())));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.getDtoAsJsonString(validationErrorResponse));
        }
    }

    @PutMapping("/films/{id}")
    public ResponseEntity<String> updateFilm(@PathVariable int id, @RequestBody Film film) {
        if (validate(film, "")) {
            films.remove(id);
            Film updatedFilm = film.toBuilder().id(id).build();
            films.put(id, updatedFilm);
            log.info("Изменен фильм {}", films.get(film.getId()));
            return ResponseEntity.status(HttpStatus.OK).body(JsonUtils.getDtoAsJsonString(films.get(id)));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.getDtoAsJsonString(validationErrorResponse));
        }

    }

    //ЭТО реализовано, чтоб просто пройти ПР по тестам, которые противоречат логике, здравому смыслу и стандартам
    //... Прошу понять и простить
    @PutMapping("/films")
    public ResponseEntity<String> updateFilm(@RequestBody String filmString) throws JsonProcessingException {
        Film film = JsonUtils.convertFromJson(filmString, Film.class);
        if (validate(film, filmString)) {
            int id = film.getId();
            if (films.containsKey(id)) {
                films.remove(id);
                Film updatedFilm = film.toBuilder().id(id).build();
                films.put(id, updatedFilm);
                log.info("Изменен фильм {}", films.get(film.getId()));
                return ResponseEntity.status(HttpStatus.OK).body(JsonUtils.getDtoAsJsonString(films.get(id)));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(JsonUtils.getDtoAsJsonString(new ValidationErrorResponse("Пользователь не найден", new ArrayList<>())));
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(JsonUtils.getDtoAsJsonString(validationErrorResponse));
        }
    }

    @GetMapping("/films")
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.status(HttpStatus.OK).body(films.values().stream().toList());
    }

    @GetMapping("/films/clear")
    public ResponseEntity<List<Film>> clearFilms() {
        films.clear();
        return ResponseEntity.status(HttpStatus.OK).body(films.values().stream().toList());
    }

    private boolean validate(Film film, String filmString) {
        boolean validated = true;
        if (filmString == null) {
            validated = false;
            validationErrorResponse.getDetails().add("Тело запроса не может быть null");
        }
        validationErrorResponse = new ValidationErrorResponse("Ошибка валидации", new ArrayList<>());
        if (film == null) {
            validated = false;
            validationErrorResponse.getDetails().add("Некорректные параметры запроса");
        }
        if (film.getName() == null || film.getName().isEmpty()) {
            validated = false;
            validationErrorResponse.getDetails().add("Название фильма не может быть null или пустым");
        }
        if (film.getDescription().length() > 200) {
            validated = false;
            validationErrorResponse.getDetails().add("Описание фильма не должно быть больше 200 символов");
        }
        if (film.getDuration() <= 0) {
            validated = false;
            validationErrorResponse.getDetails().add("Продолжительность фильма должна быть положительной");
        }
        LocalDate validDateFrom = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate().isBefore(validDateFrom)) {
            validated = false;
            validationErrorResponse.getDetails().add("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (!validated) log.error("Фильм {} не прошел валидацию", film);
        return validated;
    }
}
