package ru.yandex.practicum.filmorate.storage.genre;

import java.nio.file.Files;
import java.nio.file.Path;

public enum GenreQuery {
    SELECT_ALL("src/main/resources/sql/genre/select_all.sql"),
    SELECT_BY_ID("src/main/resources/sql/genre/select_by_id.sql"),
    INSERT("src/main/resources/sql/genre/insert.sql"),
    UPDATE("src/main/resources/sql/genre/update.sql"),
    DELETE_BY_ID("src/main/resources/sql/genre/delete_by_id.sql"),
    DELETE_BY_FILM_ID("src/main/resources/sql/genre/delete_by_film_id.sql"),
    INSERT_BY_FILM_ID("src/main/resources/sql/genre/insert_by_film_id.sql"),
    SELECT_BY_FILM_ID("src/main/resources/sql/genre/select_by_film_id.sql");

    private final String sql;

    GenreQuery(String queryPath) {
        this.sql = loadSql(queryPath);
    }

    public String getSql() {
        return sql;
    }

    private String loadSql(String path){
        try{
            return Files.readString(Path.of(path));
        } catch (java.io.IOException | NullPointerException e ) {
            throw new IllegalStateException("Не удалось загрузить SQL-файл: " + path, e);
        }
    }
}
