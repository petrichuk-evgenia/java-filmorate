package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

import static ru.yandex.practicum.filmorate.controller.UserController.userCounter;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@ToString
public class User {

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

    @Builder.Default
    private int id = ++userCounter;

    @JsonCreator
    public User(@JsonProperty("name") String name,
                @JsonProperty("birthday") LocalDate birthday,
                @JsonProperty("login") String login,
                @JsonProperty("email") String email,
                @JsonProperty("id") int id) {
        this.name = name;
        this.birthday = birthday;
        this.login = login;
        this.email = email;
        if (id == 0) {
            this.id = ++userCounter;
        } else {
            this.id = id;
        }
    }
}
