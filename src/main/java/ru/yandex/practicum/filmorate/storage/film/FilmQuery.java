package ru.yandex.practicum.filmorate.storage.film;

public enum FilmQuery {
    INSERT("""
            INSERT INTO films (name, description, release_date, duration, mpa)
            VALUES (:name, :description, :releaseDate, :duration, :mpa)
            """),
    UPDATE("""
            UPDATE films
            SET name = :name, description = :description, release_date = :releaseDate, duration = :duration, mpa = :mpa
            WHERE id = :id
            """),
    SELECT_BY_ID("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
            FROM films f
            WHERE f.id = :id
            """),
    SELECT_ALL("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
            FROM films f
            ORDER BY f.id
            """),
    DELETE_BY_ID("""
            DELETE FROM films WHERE id = :id
            """),
    SELECT_COMMON_FILMS("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
            FROM films AS f
            JOIN film_likes AS fl ON f.id = fl.film_id
            WHERE f.id IN (
                SELECT film_id FROM film_likes WHERE user_id = :userId
            )
            AND f.id IN (
                SELECT film_id FROM film_likes WHERE user_id = :friendId
            )
            GROUP BY f.id
            ORDER BY COUNT(fl.user_id) DESC
            """),
    SELECT_BY_DIRECTOR_SORTED("""
            SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                 , COALESCE(lc.cnt, 0) AS likes_count
            FROM films f
            JOIN film_directors fd ON fd.film_id = f.id
            LEFT JOIN (
               SELECT film_id, COUNT(*) AS cnt
               FROM film_likes
               GROUP BY film_id
            ) lc ON lc.film_id = f.id
            WHERE fd.director_id = :directorId
            ORDER BY %s
            """),
    SELECT_BY_TITLE_AND_DIRECTOR("""
            SELECT f.id, f.name, f.release_date, f.description, f.duration, f.mpa
            FROM films f
            LEFT JOIN (
                SELECT fl.film_id, COUNT(fl.user_id) AS likes_cnt
                FROM film_likes fl
                GROUP BY fl.film_id
            ) lc ON lc.film_id = f.id
            WHERE %s
            ORDER BY COALESCE(lc.likes_cnt, 0) DESC, f.name ASC, f.id ASC
            """);

    private final String sql;

    FilmQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
