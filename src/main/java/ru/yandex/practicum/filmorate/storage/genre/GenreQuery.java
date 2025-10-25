package ru.yandex.practicum.filmorate.storage.genre;

public enum GenreQuery {
    SELECT_ALL("""
            SELECT g.id, g.name
            FROM genres g
            ORDER BY g.id
            """),
    SELECT_BY_ID("""
            SELECT g.id, g.name
            FROM genres g
            WHERE g.id = :id
            """),
    INSERT("""
            INSERT INTO genres (id, name) VALUES (:id, :name)
            """),
    UPDATE("""
            UPDATE genres SET name = :name WHERE id = :id
            """),
    DELETE_BY_ID("""
            DELETE FROM genres WHERE id = :id
            """),
    DELETE_BY_FILM_ID("""
            DELETE FROM film_genres WHERE film_id = :filmId
            """),
    INSERT_BY_FILM_ID("""
            INSERT INTO film_genres (film_id, genre_id) VALUES (:filmId, :id)
            """),
    SELECT_BY_FILM_ID("""
            SELECT g.id, g.name
            FROM genres g
            JOIN film_genres fg ON fg.genre_id = g.id
            WHERE fg.film_id = :filmId
            ORDER BY g.id
            """);

    private final String sql;

    GenreQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}