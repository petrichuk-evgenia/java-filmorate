package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.dao.GenreDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class GenreService {

    @Autowired
    private final GenreDao genreDao;
    private List<Genre> allGenresCache;
    private Map<Long, Genre> genresByIdCache;

    public GenreService(GenreDao genreDao) {
        this.genreDao = genreDao;
        allGenresCache = genreDao.getAllGenres();
        genresByIdCache = new HashMap<>();
    }

    public List<Genre> getAllGenres() {
        for (Genre genre : allGenresCache) {
            genresByIdCache.put(genre.getId(), genre);
        }
        return allGenresCache;
    }

    public Genre getGenreById(Long id) {
        Genre genre = genreDao.getGenreById(id)
                .orElseThrow(() -> {
                    log.error("Жанр с ID {} не найден", id);
                    return new IdNotFoundException("Жанр с ID " + id + " не найден");
                });

        genresByIdCache.put(genre.getId(), genre);
        return genre;
    }

    public Map<Long, Genre> getAllGenresMap() {
        getAllGenres();
        return genresByIdCache;
    }
}
