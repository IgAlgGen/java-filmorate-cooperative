package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class FeedEvent {
    private long eventId;
    private long timestamp;
    private int userId;
    private EventType eventType;
    private Operation operation;
    private long entityId;
}