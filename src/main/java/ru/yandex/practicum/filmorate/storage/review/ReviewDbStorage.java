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
        final String sqlReviewInsert = """
                INSERT INTO reviews (content, film_Id, user_Id, is_positive)
                VALUES (:content, :filmId, :userId, :isPositive)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("filmId", review.getFilmId())
                .addValue("userId", review.getUserId())
                .addValue("isPositive", review.getIsPositive());
        namedParameterJdbcTemplate.update(sqlReviewInsert, params, keyHolder, new String[]{"id"});
        review.setReviewId(keyHolder.getKeyAs(Long.class));
        return review;
    }

    @Override
    @Transactional
    public Review updateReview(Review review) {
        final String sqlReviewUpdate = """
                UPDATE reviews
                SET content = :content, film_id = :filmId, user_id = :userId, is_positive = :isPositive
                WHERE id = :reviewId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", review.getReviewId())
                .addValue("content", review.getContent())
                .addValue("filmId", review.getFilmId())
                .addValue("userId", review.getUserId())
                .addValue("isPositive", review.getIsPositive());
        int updated = namedParameterJdbcTemplate.update(sqlReviewUpdate, params);
        if (updated == 0) {
            throw new NoSuchElementException("Отзыв не найден: id=" + review.getReviewId());
        }
        return review;
    }

    @Override
    public void deleteReview(Long id) {
        final String sqlReviewDelete = "DELETE FROM reviews WHERE id = :reviewId";
        int result = namedParameterJdbcTemplate.update(sqlReviewDelete,
                new MapSqlParameterSource("reviewId", id));
        if (result == 0) {
            throw new NotFoundException("Отзыв не найден: id=" + id);
        }
    }

    @Override
    public Optional<Review> getReviewForId(Long id) {
        final String sqlGetReviewForId = """
                SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive,
                COUNT(CASE WHEN l.is_like = TRUE THEN 1 END) AS positive_likes_count,
                COUNT(CASE WHEN l.is_like = FALSE THEN 1 END) AS negative_likes_count
                FROM reviews r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                WHERE r.id = :reviewId
                GROUP BY r.id, r.content, r.user_id, r.film_id;
                """;
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sqlGetReviewForId,
                    new MapSqlParameterSource("reviewId", id), reviewRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewsForFilmId(int filmId, int count) {
        final String sqlGetReviewsForIdFilm = """
                SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive,
                COUNT(CASE WHEN l.is_like = TRUE  THEN 1 END) AS positive_likes_count,
                COUNT(CASE WHEN l.is_like = FALSE THEN 1 END) AS negative_likes_count,
                (COUNT(CASE WHEN l.is_like = TRUE  THEN 1 END) -
                COUNT(CASE WHEN l.is_like = FALSE THEN 1 END)) AS rating_score
                FROM reviews r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                WHERE r.film_id = :filmId
                GROUP BY r.id, r.content, r.user_id, r.film_id
                ORDER BY rating_score DESC, r.id ASC
                LIMIT :count;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("count", count);
        return namedParameterJdbcTemplate.query(sqlGetReviewsForIdFilm, params, reviewRowMapper);
    }

    @Override
    public List<Review> getReviews(int count) {
        final String sqlGetReviews = """
                SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive,
                COUNT(CASE WHEN l.is_like = TRUE  THEN 1 END) AS positive_likes_count,
                COUNT(CASE WHEN l.is_like = FALSE THEN 1 END) AS negative_likes_count,
                (COUNT(CASE WHEN l.is_like = TRUE  THEN 1 END) -
                COUNT(CASE WHEN l.is_like = FALSE THEN 1 END)) AS rating_score
                FROM reviews r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                GROUP BY r.id, r.content, r.user_id, r.film_id
                ORDER BY rating_score DESC, r.id ASC
                LIMIT :count;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);
        return namedParameterJdbcTemplate.query(sqlGetReviews, params, reviewRowMapper);
    }

    @Override
    public void addLikeReview(Long id, int userId, boolean isPositive) {
        deleteLikeReview(id, userId);
        if (getReviewForId(id).isEmpty()) {
            throw new NotFoundException("Отзыв не найден id: " + id);
        }
        final String sqlLikeInsert = """
                INSERT INTO reviews_likes (review_Id, user_id, is_like)
                VALUES (:reviewId, :userId, :isPositive)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", id)
                .addValue("userId", userId)
                .addValue("isPositive", isPositive);
        namedParameterJdbcTemplate.update(sqlLikeInsert, params);
    }

    @Override
    public void deleteLikeReview(Long id, int userId) {
        if (getReviewForId(id).isEmpty()) {
            throw new NotFoundException("Отзыв не найден id: " + id);
        }
        final String sqlLikeDelete = """
                DELETE FROM reviews_likes
                WHERE review_id = :reviewId AND user_id = :userId""";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewId", id)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(sqlLikeDelete, params);
    }
}
