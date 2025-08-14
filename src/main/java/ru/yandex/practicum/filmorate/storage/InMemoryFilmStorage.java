package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

/**
 * Отдельный бин хранилища фильмов.
 */
@Component("filmStorage")
public class InMemoryFilmStorage extends InMemoryStorageImpl<Film> {
}
