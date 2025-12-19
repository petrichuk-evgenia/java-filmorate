package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@EqualsAndHashCode
@ToString
@Setter
public class User {

    private Set<Integer> friends;

    private String name;
    @NonNull
    @Past(message = "День рождения не может быть в будущем")
    private LocalDate birthday;
    @NonNull
    @NotEmpty(message = "Логин не может быть null или пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;
    @NonNull
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email должен быть в формате name@example.com")
    private String email;

    private int id;

    @Builder(toBuilder = true)
    public User(String name, @NonNull LocalDate birthday, @NonNull String login, @NonNull String email) {
        this.friends = new HashSet<>();
        if (name == null) {
            this.name = login;
        } else {
            this.name = name;
        }
        this.birthday = birthday;
        this.login = login;
        this.email = email;
    }
}
