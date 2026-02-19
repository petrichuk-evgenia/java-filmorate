package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
    List<Film> getAllFilms();

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Optional<Film> getFilmById(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getPopularFilms(int count);

    boolean existsById(Long id);

    /**
     * Получаем список ID пользаков, которые поставили лайки фильму
     */
    Set<Long> getFilmLikes(Long filmId);

    /**
     * Получаем список ID фильмов, которые лайкнул пользак
     */
    Set<Long> getUserLikes(Long userId);

    /**
     * Получаем рекомендации для пользака на основе похожих пользаков
     */
    List<Film> getRecommendations(Long userId);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);

    Film deleteFilm(Long id);
}
