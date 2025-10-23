package ru.yandex.practicum.filmorate.storage.feed;

import java.nio.file.Files;
import java.nio.file.Path;

public enum FeedQuery {
    INSERT("src/main/resources/sql/feed/insert.sql"),
    SELECT_BY_USER_ID("src/main/resources/sql/feed/select_by_user_id.sql");

    private final String sql;

    FeedQuery(String queryPath) {
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