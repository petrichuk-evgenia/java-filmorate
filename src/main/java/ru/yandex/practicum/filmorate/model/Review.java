package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;

    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "isPositive cannot be null")
    private Boolean isPositive;

    @NotNull(message = "UserId cannot be null")
    private Long userId;

    @NotNull(message = "FilmId cannot be null")
    private Long filmId;

    private Integer useful;

    private LocalDateTime createdAt;
}
