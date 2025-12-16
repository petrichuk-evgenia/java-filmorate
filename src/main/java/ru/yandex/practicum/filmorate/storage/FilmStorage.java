package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {

    Film addFilm(Film film);

    Film updateFilm(int id, Film film);

    List<Film> getAllFilms();

    List<Film> clearFilms();

    Map<Integer, Film> getFilms();
}
