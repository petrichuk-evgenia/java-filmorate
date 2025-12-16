package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping("/films")
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filmService.getFilmStorage().addFilm(film));
    }

    @PutMapping("/films/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable int id, @Valid @RequestBody Film film) {
        return ResponseEntity.status(HttpStatus.OK).body(filmService.getFilmStorage().updateFilm(id, film));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/films")
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.status(HttpStatus.OK).body(filmService.getFilmStorage().getAllFilms());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/films/clear")
    public ResponseEntity<List<Film>> clearFilms() {
        return ResponseEntity.status(HttpStatus.OK).body(filmService.getFilmStorage().clearFilms());
    }

    @PutMapping("/films/{id}/like/{userId}")
    public ResponseEntity<Map<String, String>> addLike(@Valid @PathVariable int id, @Valid @PathVariable int userId) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(filmService.addLike(id, userId));
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public ResponseEntity<Map<String, String>> deleteLike(@Valid @PathVariable int id, @Valid @PathVariable int userId) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(filmService.deleteLike(id, userId));
    }

    @GetMapping("/films/popular")
    public ResponseEntity<List<Film>> getPopularFilms(@Valid @RequestParam int count) throws CustomValidationExpression {
        return ResponseEntity.status(HttpStatus.OK).body(filmService.getFilmsByListIDs(filmService.getPopularFilms(count)));
    }

    //ЭТО реализовано, чтоб просто пройти ПР по тестам, которые противоречат логике, здравому смыслу и стандартам
    //... Прошу понять и простить
    @PutMapping("/films")
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        int id = film.getId();
        if (filmService.getFilmStorage().getFilms().containsKey(id)) {
            filmService.getFilmStorage().updateFilm(id, film);
            log.info("Изменен фильм {}", filmService.getFilmStorage().getFilms().get(film.getId()));
            return ResponseEntity.status(HttpStatus.OK).body(filmService.getFilmStorage().getFilms().get(id));
        } else {
            throw new IdNotFoundException("Фильм не найден");
        }
    }
}
