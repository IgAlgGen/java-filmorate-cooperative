package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review createReview(Review review);

    Review updateReview(Review review);

    void deleteReview(Long id);

    Optional<Review> getReviewForId(Long id);

    List<Review> getReviewsForFilmId(int filmId, int count);

    List<Review> getReviews(int count);

    void addLikeReview(Long id, int userId, boolean isPositive);

    void deleteLikeReview(Long id, int userId);

}
