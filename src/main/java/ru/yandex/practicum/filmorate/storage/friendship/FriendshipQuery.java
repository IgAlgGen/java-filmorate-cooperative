package ru.yandex.practicum.filmorate.storage.friendship;

public enum FriendshipQuery {
    INSERT("""
                MERGE INTO friendships (requester_id, addressee_id, status)
                KEY (requester_id, addressee_id) VALUES (:userId, :friendId, :status)
                """),
    UPDATE_BOTH_CONFIRMED("""
                UPDATE friendships SET status = :status
                WHERE (requester_id = :userId AND addressee_id = :friendId) OR (requester_id = :friendId AND addressee_id = :userId)
                """),
    DELETE("""
                DELETE FROM friendships
                WHERE requester_id = :userId AND addressee_id = :friendId
                """),
    UPDATE("""
                UPDATE friendships SET status = :status
                WHERE requester_id = :userId AND addressee_id = :friendId
                """),
    SELECT_FRIENDS_BY_USER_ID("""
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.id = f.addressee_id
                WHERE f.requester_id = :userId
                ORDER BY u.id
                """),
    SELECT_COMMON_FRIENDS("""
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f1
                JOIN friendships f2 ON f1.addressee_id = f2.addressee_id
                JOIN users u ON u.id = f1.addressee_id
                WHERE f1.requester_id = :userId AND f2.requester_id = :friendId
                ORDER BY u.id
                """);

    private final String sql;

    FriendshipQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}