SELECT fe.event_id, fe.timestamp, fe.user_id, fe.event_type, fe.operation, fe.entity_id
FROM feed_events fe
WHERE fe.user_id = :user_id
ORDER BY fe.timestamp;