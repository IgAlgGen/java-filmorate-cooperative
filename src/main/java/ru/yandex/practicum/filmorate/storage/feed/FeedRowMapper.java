package ru.yandex.practicum.filmorate.storage.feed;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Component
public class FeedRowMapper implements RowMapper<FeedEvent> {
    @Override
    public FeedEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        FeedEvent fe = new FeedEvent();
        fe.setEventId(rs.getLong("event_id"));
        Timestamp ts = rs.getTimestamp("timestamp");
        fe.setTimestamp(ts != null ? ts.toInstant().toEpochMilli() : 0L);
        fe.setUserId(rs.getInt("user_id"));
        fe.setEventType(EventType.valueOf(rs.getString("event_type").toUpperCase()));
        fe.setOperation(Operation.valueOf(rs.getString("operation").toUpperCase()));
        fe.setEntityId(rs.getLong("entity_id"));
        return fe;
    }
}
