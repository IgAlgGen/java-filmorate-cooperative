package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.FeedEvent;

import java.util.*;

public interface FeedStorage {
    void addEvent(FeedEvent event);

    List<FeedEvent> findByUserId(int userId);
}
