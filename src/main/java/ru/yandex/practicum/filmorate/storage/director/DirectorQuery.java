package ru.yandex.practicum.filmorate.storage.director;

public enum DirectorQuery {
    INSERT("INSERT INTO directors (name) VALUES (:name);"),
    UPDATE("UPDATE directors SET name = :name WHERE id = :id;"),
    SELECT_BY_ID("SELECT id, name FROM directors WHERE id = :id;"),
    SELECT_ALL("SELECT id, name FROM directors ORDER BY id;"),
    SELECT_BY_FILM_ID("""
            SELECT d.id, d.name
            FROM directors d
            JOIN film_directors fd ON fd.director_id = d.id
            WHERE fd.film_id = :filmId
            ORDER BY d.id;"""),
    DELETE_BY_ID("DELETE FROM directors WHERE id = :id;");

    private final String sql;

    DirectorQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
