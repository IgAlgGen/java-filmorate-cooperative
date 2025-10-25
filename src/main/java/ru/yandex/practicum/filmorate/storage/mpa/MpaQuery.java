package ru.yandex.practicum.filmorate.storage.mpa;

import java.nio.file.Files;
import java.nio.file.Path;

public enum MpaQuery {
    SELECT_ALL("src/main/resources/sql/mpa/select_all.sql"),
    SELECT_BY_ID("src/main/resources/sql/mpa/select_by_id.sql");

    private final String sql;

    MpaQuery(String queryPath) {
        this.sql = loadSql(queryPath);
    }

    public String getSql() {
        return sql;
    }

    private String loadSql(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (java.io.IOException | NullPointerException e) {
            throw new IllegalStateException("Не удалось загрузить SQL-файл: " + path, e);
        }
    }
}
