package ru.yandex.practicum.filmorate.storage.filmLike;

import java.nio.file.Files;
import java.nio.file.Path;

public enum FilmLikeQuery {
    INSERT("src/main/resources/sql/filmLike/insert.sql"),
    DELETE_BY_USER_ID_AND_FILM_ID("src/main/resources/sql/filmLike/delete_by_user_id_and_film_id.sql"),
    SELECT_POPULAR("src/main/resources/sql/filmLike/select_popular.sql"),
    SELECT_BY_USER_AND_FILM_LIKES("src/main/resources/sql/filmLike/select_by_user_and_film_likes.sql");

    private final String sql;

    FilmLikeQuery(String queryPath) {
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