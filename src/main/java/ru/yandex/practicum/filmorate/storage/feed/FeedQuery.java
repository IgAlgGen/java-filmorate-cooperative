package ru.yandex.practicum.filmorate.storage.feed;

public enum FeedQuery {
    INSERT("""
                INSERT INTO feed_events (timestamp, user_id, event_type, operation, entity_id)
                VALUES (:timestamp, :user_id, :event_type, :operation, :entity_id)
                """),
    SELECT_BY_USER_ID("""
                SELECT fe.event_id, fe.timestamp, fe.user_id, fe.event_type, fe.operation, fe.entity_id
                FROM feed_events fe
                WHERE fe.user_id = :user_id
                ORDER BY fe.timestamp
                """);

    private final String sql;

    FeedQuery(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}