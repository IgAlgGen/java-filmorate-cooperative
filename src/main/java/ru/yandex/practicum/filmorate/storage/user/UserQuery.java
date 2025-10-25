package ru.yandex.practicum.filmorate.storage.user;

public enum UserQuery {
    INSERT("""
            INSERT INTO users (email, login, name, birthday)
            VALUES (:email, :login, :name, :birthday)
            """),
    UPDATE("""
            UPDATE users SET email = :email, login = :login, name = :name, birthday = :birthday
            WHERE id = :id
            """),
    SELECT_BY_ID("""
            SELECT id, email, login, name, birthday
            FROM users
            WHERE id = :id
            """),
    SELECT_ALL("""
            SELECT id, email, login, name, birthday
            FROM users
            ORDER BY id
            """),
    DELETE_BY_ID("""
            DELETE FROM users
            WHERE id = :id
            """);

    private final String sql;

    UserQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
