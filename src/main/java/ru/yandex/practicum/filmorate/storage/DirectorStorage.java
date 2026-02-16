package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {
    List<Director> getDirectors();

    Director getDirector(Long id);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    Director deleteDirector(Long id);

}
