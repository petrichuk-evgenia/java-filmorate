package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@EqualsAndHashCode
@Builder(toBuilder = true)
@ToString
public class User {

    private String name;

    private LocalDate birthday;

    private String login;

    private String email;

    @Builder.Default
    private UUID id = UUID.randomUUID();

    @JsonCreator
    public User(@JsonProperty("name") String name,
                @JsonProperty("birthday") LocalDate birthday,
                @JsonProperty("login") String login,
                @JsonProperty("email") String email,
                @JsonProperty("id") UUID id) {
        this.name = name;
        this.birthday = birthday;
        this.login = login;
        this.email = email;
        this.id = id;
    }
}
