package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

/**
 * Отдельный бин хранилища пользователей.
 */
@Component("userStorage")
public class InMemoryUserStorage extends InMemoryStorageImpl<User> {
}
