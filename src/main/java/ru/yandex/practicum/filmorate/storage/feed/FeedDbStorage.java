package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
@Qualifier("feedDbStorage")
public class FeedDbStorage implements FeedStorage {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FeedRowMapper feedRowMapper;

    @Override
    @Transactional
    public void addEvent(FeedEvent event) {
        String sql = """
                INSERT INTO feed_events (timestamp, user_id, event_type, operation, entity_id)
                VALUES (:timestamp, :user_id, :event_type, :operation, :entity_id)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("timestamp", new Timestamp(event.getTimestamp()))
                .addValue("user_id", event.getUserId())
                .addValue("event_type", event.getEventType().name())
                .addValue("operation", event.getOperation().name())
                .addValue("entity_id", event.getEntityId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(sql, params, keyHolder, new String[]{"event_id"});
        Number key = keyHolder.getKey();
        event.setEventId(Objects.requireNonNull(key).longValue());
    }

    @Override
    public List<FeedEvent> findByUserId(int userId) {
        final String sqlFeedSelectById = """
                SELECT fe.event_id, fe.timestamp, fe.user_id, fe.event_type, fe.operation, fe.entity_id
                FROM feed_events fe
                WHERE fe.user_id = :user_id
                ORDER BY fe.timestamp
                """;
            return namedParameterJdbcTemplate.query(sqlFeedSelectById,
                    new MapSqlParameterSource("user_id", userId), feedRowMapper);
    }
}
