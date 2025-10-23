package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.storage.feed.FeedStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    @Qualifier("feedDbStorage")
    private final FeedStorage feedStorage;

    @Qualifier("userDbStorage")
    private  final UserStorage userStorage;

    public List<FeedEvent> getFeed(int userId) {
        log.debug("Получение ленты событий пользователя ID {}", userId);
        userStorage.getById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с ID %d не найден", userId)));
        List<FeedEvent> events = feedStorage.findByUserId(userId);
        log.debug("Найдено {} событий", events.size());
        return events;
    }
}
