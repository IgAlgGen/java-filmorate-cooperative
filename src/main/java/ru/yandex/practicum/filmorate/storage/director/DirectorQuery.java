package ru.yandex.practicum.filmorate.storage.director;

import java.nio.file.Files;
import java.nio.file.Path;

public enum DirectorQuery {
    INSERT("src/main/resources/sql/director/insert.sql"),
    UPDATE("src/main/resources/sql/director/update.sql"),
    SELECT_BY_ID("src/main/resources/sql/director/select_by_id.sql"),
    SELECT_ALL("src/main/resources/sql/director/select_all.sql"),
    SELECT_BY_FILM_ID("src/main/resources/sql/director/select_by_film_id.sql"),
    DELETE_BY_ID("src/main/resources/sql/director/delete_by_id.sql");

    private final String sql;

    DirectorQuery(String queryPath) {
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
