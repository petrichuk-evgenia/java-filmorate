package ru.yandex.practicum.filmorate.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.ReviewDbStorage;

import java.util.List;

@Slf4j
@Service
public class ReviewService {
    private final ReviewDbStorage reviewRepository;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    public ReviewService(@Qualifier("reviewDbStorage") ReviewDbStorage reviewRepository,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage,
                         @Qualifier("eventDbStorage") EventStorage eventStorage) {
        this.reviewRepository = reviewRepository;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventStorage = eventStorage;
    }

    @Transactional
    public Review addReview(Review review) {
        if (!userStorage.existsById(review.getUserId())) {
            log.error("Пользователь с ID {} не найден", review.getUserId());
            throw new IdNotFoundException("User not found: " + review.getUserId());
        }

        if (!filmStorage.existsById(review.getFilmId())) {
            log.error("Фильм с ID {} не найден", review.getFilmId());
            throw new IdNotFoundException("Film not found: " + review.getFilmId());
        }

        Review savedReview = reviewRepository.save(review);

        Event event = Event.builder()
                .userId(savedReview.getUserId())
                .timestamp(System.currentTimeMillis())
                .eventType(EventType.REVIEW)
                .operation(Operation.ADD)
                .entityId(savedReview.getReviewId())
                .build();
        eventStorage.addEvent(event);

        log.info("Отзыв с ID {} успешно создан", savedReview.getReviewId());
        return savedReview;
    }

    @Transactional
    public Review updateReview(Review review) {
        Review existingReview = reviewRepository.getById(review.getReviewId())
                .orElseThrow(() -> new IdNotFoundException("Review not found: " + review.getReviewId()));

        Review updatedReview = reviewRepository.update(review);

        Event event = Event.builder()
                .userId(updatedReview.getUserId())
                .timestamp(System.currentTimeMillis())
                .eventType(EventType.REVIEW)
                .operation(Operation.UPDATE)
                .entityId(updatedReview.getReviewId())
                .build();
        eventStorage.addEvent(event);

        log.info("Отзыв с ID {} успешно обновлен", updatedReview.getReviewId());
        return updatedReview;
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.getById(id)
                .orElseThrow(() -> new IdNotFoundException("Review not found: " + id));

        reviewRepository.deleteById(id);

        Event event = Event.builder()
                .userId(review.getUserId())
                .timestamp(System.currentTimeMillis())
                .eventType(EventType.REVIEW)
                .operation(Operation.REMOVE)
                .entityId(review.getReviewId())
                .build();
        eventStorage.addEvent(event);

        log.info("Отзыв с ID {} успешно удален", id);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.getById(id)
                .orElseThrow(() -> new IdNotFoundException("Review not found: " + id));
    }

    public List<Review> getReviewsByFilm(Long filmId, int count) {
        if (count <= 0) {
            count = 10;
        }

        if (filmId != null && filmId > 0) {
            if (!filmStorage.existsById(filmId)) {
                throw new IdNotFoundException("Film not found: " + filmId);
            }
            return reviewRepository.getByFilmId(filmId, count);
        } else {
            return reviewRepository.getAll(count);
        }
    }

    @Transactional
    public void addLike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        updateFeedback(reviewId, userId, true);
        log.info("Пользователь {} поставил лайк отзыву {}", userId, reviewId);
    }

    @Transactional
    public void addDislike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        updateFeedback(reviewId, userId, false);
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, reviewId);
    }

    @Transactional
    public void removeLike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        removeFeedback(reviewId, userId, true);
        log.info("Пользователь {} удалил лайк с отзыва {}", userId, reviewId);
    }

    @Transactional
    public void removeDislike(Long reviewId, Long userId) {
        validateUserAndReview(reviewId, userId);
        removeFeedback(reviewId, userId, false);
        log.info("Пользователь {} удалил дизлайк с отзыва {}", userId, reviewId);
    }

    private void validateUserAndReview(Long reviewId, Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new IdNotFoundException("User not found: " + userId);
        }
        if (!reviewRepository.getById(reviewId).isPresent()) {
            throw new IdNotFoundException("Review not found: " + reviewId);
        }
    }

    private void updateFeedback(Long reviewId, Long userId, boolean isLike) {
        String sql = "MERGE INTO review_feedback (review_id, user_id, is_like) " +
                "KEY (review_id, user_id) VALUES (?, ?, ?)";
        reviewRepository.getJdbcTemplate().update(sql, reviewId, userId, isLike);
        reviewRepository.recalculateUseful(reviewId);
    }

    private void removeFeedback(Long reviewId, Long userId, boolean isLike) {
        String sql = "DELETE FROM review_feedback WHERE review_id = ? AND user_id = ? AND is_like = ?";
        reviewRepository.getJdbcTemplate().update(sql, reviewId, userId, isLike);
        reviewRepository.recalculateUseful(reviewId);
    }
}