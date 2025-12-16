package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.HashMap;
import java.util.List;

@Component
@NoArgsConstructor
@Slf4j
@Getter
public class InMemoryFilmStorage implements FilmStorage {

    public static int nextId = 0;
    private final HashMap<Integer, Film> films = new HashMap<>();

    @Override
    public Film addFilm(Film film) {
        nextId++;
        film.setId(nextId);
        films.put(film.getId(), film);
        log.info("Добавлен фильм {}", films.get(film.getId()));
        return films.get(film.getId());
    }

    @Override
    public Film updateFilm(int id, Film film) {
        film.setId(id);
        films.put(id, film);
        log.info("Изменен фильм {}", films.get(film.getId()));
        return films.get(film.getId());
    }

    @Override
    public List<Film> getAllFilms() {
        return films.values().stream().toList();
    }

    @Override
    public List<Film> clearFilms() {
        films.clear();
        nextId = 0;
        return films.values().stream().toList();
    }
}
