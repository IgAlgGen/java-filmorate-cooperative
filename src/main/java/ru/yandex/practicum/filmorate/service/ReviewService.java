package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    @Qualifier("ReviewDbStorage")
    private final ReviewStorage reviewStorage;

    public Review createNewReview(Review review) {
        if (filmStorage.getById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм с id: " + review.getFilmId() + " не найден.");
        }
        if (userStorage.getById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id: " + review.getUserId() + " не найден.");
        }
        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review review) {
        if (reviewStorage.getReviewForId(review.getReviewId()).isEmpty()) {
            throw new NotFoundException("Отзыв с id: " + review.getReviewId() + " не найден.");
        }
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Long id) {
        reviewStorage.deleteReview(id);
    }

    public Review getReviewForId(Long id) {
        return reviewStorage.getReviewForId(id).orElseThrow(() ->
                new NotFoundException("Отзыв с id: " + id + " не найден."));
    }

    public List<Review> getReviewsForFilmId(int filmId, int count) {
        if (filmId == 0) {
            return reviewStorage.getReviews(count);
        } else {
            return reviewStorage.getReviewsForFilmId(filmId, count);
        }
    }

    public void addLikeReview(Long id, int userId) {
        reviewStorage.addLikeReview(id, userId, true);
    }

    public void addDislikeReview(Long id, int userId) {
        reviewStorage.addLikeReview(id, userId, false);
    }

    public void deleteLikeReview(Long id, int userId) {
        reviewStorage.deleteLikeReview(id, userId);
    }

}
