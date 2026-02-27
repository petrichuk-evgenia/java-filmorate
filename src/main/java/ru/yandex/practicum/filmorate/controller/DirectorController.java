package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RequestMapping("/directors")
@RestController
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public List<Director> getDirectors() {
        log.info("GET /directors - получение списка режиссеров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirector(@PathVariable Long id) {
        log.info("GET /directors/{} - получение режиссера по ID", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@RequestBody @NonNull @Valid Director director) {
        log.info("POST /directors - добавление режиссера");
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("PUT /directors - изменение режиссера");
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public Director deleteDirector(@PathVariable Long id) {
        log.info("DELETE /directors/{} - удаление режиссера по ID", id);
        return directorService.deleteDirector(id);
    }
}
