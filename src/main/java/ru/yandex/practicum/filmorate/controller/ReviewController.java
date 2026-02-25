package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("POST /reviews - создание отзыва");
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("PUT /reviews - обновление отзыва");
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("DELETE /reviews/{} - удаление отзыва", id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable Long id) {
        log.info("GET /reviews/{} - получение отзыва по ID", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {
        if (filmId != null) {
            log.info("GET /reviews?filmId={}&count={} - получение отзывов к фильму", filmId, count);
        } else {
            log.info("GET /reviews?count={} - получение самых популярных отзывов", count);
        }
        return reviewService.getReviewsByFilm(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT /reviews/{}/like/{} - пользователь поставил лайк отзыву", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT /reviews/{}/dislike/{} - пользователь поставил дизлайк отзыву", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("DELETE /reviews/{}/like/{} - пользователь удалил лайк", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("DELETE /reviews/{}/dislike/{} - пользователь удалил дизлайк", id, userId);
        reviewService.removeDislike(id, userId);
    }
}