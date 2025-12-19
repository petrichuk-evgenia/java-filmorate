package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.yandex.practicum.filmorate.annotations.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@EqualsAndHashCode
@ToString
@Setter
public class Film {

    private Set<User> likes;

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

    private int id;

    @Builder(toBuilder = true)
    public Film(@NonNull String name, String description, @NonNull LocalDate releaseDate, @NonNull long duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.likes = new HashSet<>();
    }
}
