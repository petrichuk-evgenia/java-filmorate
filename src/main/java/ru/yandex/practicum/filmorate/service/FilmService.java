package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Getter
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Map<String, String> addLike(int filmId, int userId) {
        validateIds(filmId, userId);
        Map<String, String> response = new HashMap<>();
        if (userStorage.getUsers().containsKey(userId) && filmStorage.getFilms().containsKey(filmId)) {
            filmStorage.getFilms().get(filmId).getLikes().add(userStorage.getUsers().get(userId));
            response.put("message", String.format("Пользователь %s поставил лайк фильму %s",
                    userStorage.getUsers().get(userId).getName(), filmStorage.getFilms().get(filmId).getName()));
        }
        return response;
    }

    public Map<String, String> deleteLike(int filmId, int userId) {
        validateIds(filmId, userId);
        Map<String, String> response = new HashMap<>();
        if (userStorage.getUsers().containsKey(userId) && filmStorage.getFilms().containsKey(filmId)) {
            filmStorage.getFilms().get(filmId).getLikes().remove(userStorage.getUsers().get(userId));
            response.put("message", String.format("Пользователь %s убрал лайк у фильма %s",
                    userStorage.getUsers().get(userId).getName(), filmStorage.getFilms().get(filmId).getName()));
        }
        return response;
    }

    public List<Integer> getPopularFilms(int count) {
        if (count == 0) {
            count = 10;
        }
        Map<Integer, Integer> filmIdLikes = new TreeMap<>();
        filmStorage.getFilms().forEach((filmId, film) -> {
            filmIdLikes.put(filmId, film.getLikes().size());
        });

        return filmIdLikes.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByListIDs(List<Integer> ids) {
        List<Film> filmList = new ArrayList<>();
        ids.forEach(id -> {
            filmList.add(filmStorage.getFilms().get(id));
        });
        return filmList;
    }

    private void validateIds(int filmId, int userId) {
        /*if (filmId == 0 || userId == 0) {
            throw new CustomValidationExpression("Один из идентификаторов равен 0");
        }*/
        if (!userStorage.getUsers().containsKey(userId)) {
            throw new IdNotFoundException(String.format("Пользователь с id=%d не найден", userId));
        }
        if (!filmStorage.getFilms().containsKey(filmId)) {
            throw new IdNotFoundException(String.format("Фильм с id=%d не найден", filmId));
        }
    }

}
