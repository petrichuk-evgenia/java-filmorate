package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.controller.FilmController.filmCounter;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Film {

    private String name;

    @Builder.Default
    private String description = "";
    private LocalDate releaseDate;
    private double duration;

    @Builder.Default
    private int id = ++filmCounter;

    @JsonCreator
    public Film(@JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("releaseDate") LocalDate releaseDate,
                @JsonProperty("duration") double duration,
                @JsonProperty("id") int id) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        if (id == 0) {
            this.id = ++filmCounter;
        } else {
            this.id = id;
        }
    }
}
