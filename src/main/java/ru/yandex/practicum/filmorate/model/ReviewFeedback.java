package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFeedback {
    private Long reviewId;
    private Long userId;
    private Boolean isLike; // true = like (полезно), false = dislike (бесполезно)
}
