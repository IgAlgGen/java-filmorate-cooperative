package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ReviewRowMapper reviewRowMapper;

    @Override
    @Transactional
    public Review createReview(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("filmId", review.getFilmId())
                .addValue("userId", review.getUserId())
                .addValue("isPositive", review.getIsPositive());
        namedParameterJdbcTemplate.update(ReviewQuery.CREATE_REVIEW.getSql(), params, keyHolder, new String[]{"id"});
        review.setReviewId(keyHolder.getKeyAs(Long.class));
        return review;
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", review.getReviewId())
                .addValue("content", review.getContent())
                .addValue("isPositive", review.getIsPositive());
        int updated = namedParameterJdbcTemplate.update(ReviewQuery.UPDATE_REVIEW.getSql(), params);
        if (updated == 0) {
            throw new NoSuchElementException("Отзыв не найден: id=" + review.getReviewId());
        }
        return review;
    }

    @Override
    public void deleteReview(Long id) {
        int result = namedParameterJdbcTemplate.update(ReviewQuery.DELETE_REVIEW.getSql(),
                new MapSqlParameterSource("reviewId", id));
        if (result == 0) {
            throw new NotFoundException("Отзыв не найден: id=" + id);
        }
    }

    @Override
    public Optional<Review> getReviewForId(Long id) {
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(ReviewQuery.GET_REVIEWS_BY_FILM_ID.getSql(),
                    new MapSqlParameterSource("reviewId", id), reviewRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewsForFilmId(int filmId, int count) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("count", count);
        return namedParameterJdbcTemplate.query(ReviewQuery.GET_REVIEWS_BY_FILM_ID.getSql(), params, reviewRowMapper);
    }

    @Override
    public List<Review> getReviews(int count) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);
        return namedParameterJdbcTemplate.query(ReviewQuery.GET_ALL_REVIEWS.getSql(), params, reviewRowMapper);
    }

    @Override
    public void addLikeReview(Long id, int userId, boolean isPositive) {
        deleteLikeReview(id, userId);
        if (getReviewForId(id).isEmpty()) {
            throw new NotFoundException("Отзыв не найден id: " + id);
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", id)
                .addValue("userId", userId)
                .addValue("isPositive", isPositive);
        namedParameterJdbcTemplate.update(ReviewQuery.ADD_REVIEW_LIKE.getSql(), params);
    }

    @Override
    public void deleteLikeReview(Long id, int userId) {
        if (getReviewForId(id).isEmpty()) {
            throw new NotFoundException("Отзыв не найден id: " + id);
        }
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", id)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(ReviewQuery.REMOVE_REVIEW_LIKE.getSql(), params);
    }
}
