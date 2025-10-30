package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
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
    @Qualifier("feedDbStorage")
    private final FeedStorage feedStorage;

    public Review createNewReview(Review review) {
        log.debug("Пользователь с ID {} создает отзыв к фильму с ID {}", review.getUserId(), review.getFilmId());
        if (filmStorage.getById(review.getFilmId()).isEmpty()) {
            throw new NotFoundException("Фильм с id: " + review.getFilmId() + " не найден.");
        }
        if (userStorage.getById(review.getUserId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id: " + review.getUserId() + " не найден.");
        }
        Review created = reviewStorage.createReview(review);
        addToFeed(created.getUserId(), created.getReviewId(), EventType.REVIEW, Operation.ADD);
        log.debug("Пользователь с ID {} создал отзыв фильму с ID {} (событие добавлено в ленту)", review.getUserId(),
                review.getFilmId());
        return created;
    }

    public Review updateReview(Review review) {
        log.debug("Пользователь с ID {} обновляет отзыв к фильму с ID {}", review.getUserId(), review.getFilmId());
        Review db = reviewStorage.getReviewForId(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв с id: " + review.getReviewId() + " не найден."));

        db.setContent(review.getContent());
        db.setIsPositive(review.getIsPositive());

        reviewStorage.updateReview(db);
        addToFeed(db.getUserId(), db.getReviewId(), EventType.REVIEW, Operation.UPDATE);
        log.debug("Пользователь с ID {} обновил отзыв фильму с ID {} (событие добавлено в ленту)", review.getUserId(),
                review.getFilmId());
        return reviewStorage.getReviewForId(db.getReviewId()).orElseThrow();
    }

    public void deleteReview(Long id) {
        log.debug("Удаление отзыва с ID {}", id);
        Review review = reviewStorage.getReviewForId(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id: " + id + " не найден."));
        reviewStorage.deleteReview(id);
        addToFeed(review.getUserId(), id, EventType.REVIEW, Operation.REMOVE);
        log.debug("Отзыв с ID {} удалён пользователем {} (событие добавлено в ленту)", id, review.getUserId());
    }

    public Review getReviewForId(Long id) {
        return reviewStorage.getReviewForId(id).orElseThrow(() ->
                new NotFoundException("Отзыв с id: " + id + " не найден."));
    }

    public List<Review> getReviewsForFilmId(int filmId, int count) {
        if (filmId == 0) {
            return reviewStorage.getReviews(count);
        }
        return reviewStorage.getReviewsForFilmId(filmId, count);
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

    private void addToFeed(int userId, Long entityId, EventType type, Operation op) {
        FeedEvent event = new FeedEvent();
        event.setUserId(userId);
        event.setEntityId(entityId);
        event.setEventType(type);
        event.setOperation(op);
        event.setTimestamp(Instant.now().toEpochMilli());
        feedStorage.addEvent(event);
    }

}
