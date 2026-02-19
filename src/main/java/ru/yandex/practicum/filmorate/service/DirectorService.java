package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.impl.DirectorDbStorageImpl;

import java.util.List;

@Service
public class DirectorService {
    private DirectorDbStorageImpl directorDbStorage;

    public DirectorService(DirectorDbStorageImpl directorDbStorage) {
        this.directorDbStorage = directorDbStorage;
    }

    public List<Director> getAllDirectors() {
        return directorDbStorage.getDirectors();
    }

    public Director getDirectorById(Long id) {
        return directorDbStorage.getDirector(id);
    }

    public Director createDirector(Director director) {
        return directorDbStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorDbStorage.updateDirector(director);
    }

    public Director deleteDirector(Long id) {
        return directorDbStorage.deleteDirector(id);
    }
}
