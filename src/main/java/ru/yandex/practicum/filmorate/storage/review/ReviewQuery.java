package ru.yandex.practicum.filmorate.storage.review;

public enum ReviewQuery {
    CREATE_REVIEW("""
            INSERT INTO reviews (content, film_Id, user_Id, is_positive)
            VALUES (:content, :filmId, :userId, :isPositive)
            """),
    UPDATE_REVIEW("""
            UPDATE reviews
            SET content = :content, is_positive = :isPositive
            WHERE id = :reviewId
            """),
    DELETE_REVIEW("DELETE FROM reviews WHERE id = :reviewId"),
    GET_REVIEW_BY_ID("""
                SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive,
                COUNT(CASE WHEN l.is_like = TRUE THEN 1 END) AS positive_likes_count,
                COUNT(CASE WHEN l.is_like = FALSE THEN 1 END) AS negative_likes_count
                FROM reviews r
                LEFT JOIN reviews_likes l ON r.id = l.review_id
                WHERE r.id = :reviewId
                GROUP BY r.id, r.content, r.user_id, r.film_id;
                """),
    GET_REVIEWS_BY_FILM_ID("""
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
                """),
    GET_ALL_REVIEWS("""
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
                """),
    ADD_REVIEW_LIKE("""
                INSERT INTO reviews_likes (review_Id, user_id, is_like)
                VALUES (:reviewId, :userId, :isPositive)
                """),
    REMOVE_REVIEW_LIKE("""
                DELETE FROM reviews_likes
                WHERE review_id = :reviewId AND user_id = :userId
                """);

    private final String sql;

    ReviewQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}