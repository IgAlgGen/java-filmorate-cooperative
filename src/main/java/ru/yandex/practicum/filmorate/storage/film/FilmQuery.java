package ru.yandex.practicum.filmorate.storage.film;

import java.nio.file.Files;
import java.nio.file.Path;

public enum FilmQuery {
    INSERT("src/main/resources/sql/film/insert.sql"),
    UPDATE("src/main/resources/sql/film/update.sql"),
    SELECT_ALL("src/main/resources/sql/film/select_all.sql"),
    SELECT_BY_ID("src/main/resources/sql/film/select_by_id.sql"),
    DELETE_BY_ID("src/main/resources/sql/film/delete_by_id.sql"),
    SELECT_BY_TITLE_AND_DIRECTOR("src/main/resources/sql/film/select_by_title_and_director.sql"),
    SELECT_COMMON_FILMS("src/main/resources/sql/film/select_common_films.sql"),
    SELECT_BY_DIRECTOR_SORTED("src/main/resources/sql/film/select_by_director_sorted.sql");

    private final String sql;

    FilmQuery(String queryPath) {
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
