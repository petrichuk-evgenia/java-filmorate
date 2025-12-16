package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotations.ValidReleaseDate;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.controller.FilmController.filmCounter;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Film {

    @NonNull
    @NotEmpty(message = "Название фильма не может быть null или пустым")
    private String name;

    @Length(min = 0, max = 200, message = "Описание фильма не должно быть больше 200 символов")
    private String description = "";

    @NonNull
    @ValidReleaseDate
    private LocalDate releaseDate;

    @NonNull
    @Min(value = 1, message = "Продолжительность фильма должна быть положительной")
    private long duration;

    @Builder.Default
    private int id = ++filmCounter;

    @JsonCreator
    public Film(@JsonProperty("name") String name,
                @JsonProperty("description") String description,
                @JsonProperty("releaseDate") LocalDate releaseDate,
                @JsonProperty("duration") long duration,
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
