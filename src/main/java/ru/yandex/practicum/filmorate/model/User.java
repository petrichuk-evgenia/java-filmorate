package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private LocalDate birthday;

    @NonNull
    private String login;

    @NonNull
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
