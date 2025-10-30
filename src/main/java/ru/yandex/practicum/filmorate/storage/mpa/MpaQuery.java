package ru.yandex.practicum.filmorate.storage.mpa;

public enum MpaQuery {
    SELECT_ALL("""
            SELECT m.id, m.name
            FROM mpa_ratings m
            ORDER BY m.id
            """),
    SELECT_BY_ID("""
            SELECT m.id, m.name
            FROM mpa_ratings m
            WHERE m.id = :id
            """);

    private final String sql;

    MpaQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}