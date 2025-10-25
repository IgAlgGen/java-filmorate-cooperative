package ru.yandex.practicum.filmorate.storage.friendship;

import java.nio.file.Files;
import java.nio.file.Path;

public enum FriendshipQuery {
    INSERT("src/main/resources/sql/friendship/insert.sql"),
    UPDATE_BOTH_CONFIRMED("src/main/resources/sql/friendship/update_both_confirmed.sql"),
    DELETE("src/main/resources/sql/friendship/delete.sql"),
    UPDATE("src/main/resources/sql/friendship/update.sql"),
    SELECT_COMMON_FRIENDS("src/main/resources/sql/friendship/select_common_friends.sql"),
    SELECT_FRIENDS_BY_USER_ID("src/main/resources/sql/friendship/select_friends_by_user_id.sql");


    private final String sql;

    FriendshipQuery(String queryPath) {
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