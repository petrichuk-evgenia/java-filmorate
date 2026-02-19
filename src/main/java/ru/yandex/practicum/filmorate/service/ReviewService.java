package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IdNotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.impl.ReviewDbStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewDbStorage reviewRepository;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(ReviewDbStorage reviewRepository,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewRepository = reviewRepository;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review addReview(Review review) {
        if (!userStorage.existsById(review.getUserId()) || review.getUserId() <= 0) {
            throw new IdNotFoundException("User not found: " + review.getUserId());
        }
        if (!filmStorage.existsById(review.getFilmId()) || review.getFilmId() <= 0) {
            throw new IdNotFoundException("Film not found: " + review.getUserId());
        }
        return reviewRepository.save(review);
    }

    public Review updateReview(Review review) {
        if (reviewRepository.getById(review.getReviewId()).isEmpty()) {
            throw new IdNotFoundException("Review not found");
        }
        return reviewRepository.update(review);
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

    public Review getReviewById(Long id) {
        return reviewRepository.getById(id)
                .orElseThrow(() -> new IdNotFoundException("Review not found"));
    }

    public List<Review> getReviewsByFilm(Long filmId, int count) {
        return (filmId != null && filmId > 0)
                ? reviewRepository.getByFilmId(filmId, count)
                : reviewRepository.getAll(count);
    }

    public void addLike(Long reviewId, Long userId) {
        updateFeedback(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        updateFeedback(reviewId, userId, false);
    }

    public void removeLike(Long reviewId, Long userId) {
        removeFeedback(reviewId, userId, true);
    }

    public void removeDislike(Long reviewId, Long userId) {
        removeFeedback(reviewId, userId, false);
    }

    private void updateFeedback(Long reviewId, Long userId, boolean isLike) {
        String sql = "MERGE INTO review_feedback (review_id, user_id, is_like) KEY (review_id, user_id) VALUES (?, ?, ?)";
        reviewRepository.getJdbcTemplate().update(sql, reviewId, userId, isLike);
        reviewRepository.recalculateUseful(reviewId);
    }

    private void removeFeedback(Long reviewId, Long userId, boolean isLike) {
        String sql = "DELETE FROM review_feedback WHERE review_id = ? AND user_id = ? AND is_like = ?";
        reviewRepository.getJdbcTemplate().update(sql, reviewId, userId, isLike);
        reviewRepository.recalculateUseful(reviewId);
    }
}