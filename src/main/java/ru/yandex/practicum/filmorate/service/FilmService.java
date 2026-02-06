package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.CustomValidationExpression;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final FilmDataLoader filmDataLoader;
    private final MpaService mpaService;
    private final GenreService genreService;
    private final UserService userService;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            FilmDataLoader filmDataLoader,
            MpaService mpaService,
            GenreService genreService,
            UserService userService) {
        this.filmStorage = filmStorage;
        this.filmDataLoader = filmDataLoader;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.userService = userService;
    }

    public List<Film> getAllFilms() {
        log.debug("Запрос на получение всех фильмов");
        List<Film> films = filmStorage.getAllFilms();
        return enrichFilmsWithAdditionalData(films);
    }

    public Film getFilmById(Long id) {
        log.debug("Запрос на получение фильма с ID: {}", id);
        Film film = filmStorage.getFilmById(id)
                .orElseThrow(() -> new IdNotFoundException("Фильм с ID " + id + " не найден"));

        return enrichFilmWithAdditionalData(film);
    }

    @Transactional
    public Film createFilm(Film film) {
        log.info("Создание нового фильма: {}", film.getName());
        validateFilm(film);

        validateAndEnrichFilmData(film);

        Film createdFilm = filmStorage.createFilm(film);
        log.info("Фильм создан с ID: {}", createdFilm.getId());

        return getFilmById(createdFilm.getId());
    }

    @Transactional
    public Film updateFilm(Film film) {
        log.info("Обновление фильма с ID: {}", film.getId());
        validateFilm(film);

        if (film.getId() == null) {
            log.error("Попытка обновить фильм без ID");
            throw new CustomValidationExpression("ID фильма должен быть указан");
        }

        if (!filmStorage.existsById(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new IdNotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        validateAndEnrichFilmData(film);

        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм с ID {} успешно обновлен", film.getId());

        return getFilmById(updatedFilm.getId());
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);

        if (!filmStorage.existsById(filmId)) {
            log.error("Фильм с ID {} не найден", filmId);
            throw new IdNotFoundException("Фильм с ID " + filmId + " не найден");
        }

        userService.getUserById(userId);

        filmStorage.addLike(filmId, userId);
        log.info("Лайк успешно добавлен");
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка фильму {} от пользователя {}", filmId, userId);

        if (!filmStorage.existsById(filmId)) {
            log.error("Фильм с ID {} не найден", filmId);
            throw new IdNotFoundException("Фильм с ID " + filmId + " не найден");
        }

        userService.getUserById(userId);

        filmStorage.removeLike(filmId, userId);
        log.info("Лайк успешно удален");
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = (count != null && count > 0) ? count : 10;
        log.info("Запрос на получение {} популярных фильмов", limit);

        List<Film> popularFilms = filmStorage.getPopularFilms(limit);
        return enrichFilmsWithAdditionalData(popularFilms);
    }

    private List<Film> enrichFilmsWithAdditionalData(List<Film> films) {
        if (films.isEmpty()) {
            return films;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        Map<Long, Set<Long>> filmGenreIdsMap = filmDataLoader.loadGenresForFilms(filmIds);
        Map<Long, Genre> allGenresMap = genreService.getAllGenresMap();

        Map<Long, Set<Long>> filmLikesMap = filmDataLoader.loadLikesForFilms(filmIds);

        Map<Long, Mpa> allMpaMap = mpaService.getAllMpaMap();

        for (Film film : films) {

            Set<Long> genreIds = filmGenreIdsMap.getOrDefault(film.getId(), new LinkedHashSet<>());
            Set<Genre> genres = genreIds.stream()
                    .map(allGenresMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            film.setGenres(genres);

            film.setLikes(filmLikesMap.getOrDefault(film.getId(), new HashSet<>()));

            if (film.getMpa() != null && film.getMpa().getId() != null) {
                Mpa fullMpa = allMpaMap.get(film.getMpa().getId());
                if (fullMpa != null) {
                    film.setMpa(fullMpa);
                } else {
                    film.setMpa(mpaService.getMpaById(film.getMpa().getId()));
                }
            }
        }

        return films;
    }

    private Film enrichFilmWithAdditionalData(Film film) {
        Set<Long> genreIds = filmDataLoader.loadGenresForFilm(film.getId());
        Map<Long, Genre> allGenresMap = genreService.getAllGenresMap();

        Set<Genre> genres = genreIds.stream()
                .map(allGenresMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenres(genres);

        film.setLikes(filmDataLoader.loadLikesForFilm(film.getId()));

        film.setMpa(mpaService.getMpaById(film.getMpa().getId()));

        return film;
    }

    private void validateAndEnrichFilmData(Film film) {
        Mpa mpa = mpaService.getMpaById(film.getMpa().getId());
        film.setMpa(mpa);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Genre> allGenresMap = genreService.getAllGenresMap();
            Set<Genre> validatedGenres = new LinkedHashSet<>();

            for (Long genreId : genreIds) {
                Genre genre = allGenresMap.get(genreId);
                if (genre == null) {
                    log.error("Жанр с ID {} не найден", genreId);
                    throw new IdNotFoundException("Жанр с ID " + genreId + " не найден");
                }
                validatedGenres.add(genre);
            }

            film.setGenres(validatedGenres);
            validatedGenres.stream()
                    .map(Genre::getName)
                    .collect(Collectors.toList());
        } else {
            film.setGenres(new LinkedHashSet<>());
        }
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new CustomValidationExpression("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new CustomValidationExpression("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null) {
            throw new CustomValidationExpression("Дата релиза должна быть указана");
        }

        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            throw new CustomValidationExpression("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new CustomValidationExpression("Продолжительность фильма должна быть положительным числом");
        }
    }
}
