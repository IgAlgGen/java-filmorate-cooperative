package ru.yandex.practicum.filmorate.storage.filmLike;

public enum FilmLikeQuery {
    INSERT("""
            MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (:filmId, :userId)
            """),
    DELETE_BY_USER_ID_AND_FILM_ID("""
            DELETE FROM film_likes WHERE film_id = :filmId AND user_id = :userId
            """),
    SELECT_POPULAR("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
            FROM films f
            LEFT JOIN film_likes  fl ON fl.film_id = f.id
            LEFT JOIN film_genres fg ON fg.film_id  = f.id
            WHERE (:genreId IS NULL OR fg.genre_id = :genreId)
              AND (:year    IS NULL OR YEAR(f.release_date) = :year)
            GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa
            ORDER BY COUNT(fl.user_id) DESC, f.id ASC
            LIMIT :limit
            """),
    SELECT_LIKES("""
            SELECT user_id, film_id
            FROM film_likes
            ORDER BY user_id
            """);

    private final String sql;

    FilmLikeQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}