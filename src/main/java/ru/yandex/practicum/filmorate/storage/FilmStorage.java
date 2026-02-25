package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
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

    List<Film> getPopularFilms(int count, Integer genreId, Integer year);

    boolean existsById(Long id);

    /**
     * Возвращает список идентификаторов пользователей, поставивших лайк фильму.
     */
    Set<Long> getFilmLikes(Long filmId);

    /**
     * Возвращает список идентификаторов фильмов, которым пользователь поставил лайк.
     */
    Set<Long> getUserLikes(Long userId);

    /**
     * Возвращает список рекомендованных фильмов для пользователя на основе предпочтений схожих пользователей.
     */
    List<Film> getRecommendations(Long userId);

    List<Film> getFilmsByDirector(Long directorId, String sortBy);

    List<Film> searchFilms(String query, String by);

    Film deleteFilm(Long id);

    List<Director> getDirectorsForFilm(Long filmId);
}
