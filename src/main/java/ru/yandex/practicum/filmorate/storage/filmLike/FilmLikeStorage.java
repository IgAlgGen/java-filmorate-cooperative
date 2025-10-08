package ru.yandex.practicum.filmorate.storage.filmLike;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmLikeStorage {
    void addLike(int filmId, int userId);

    boolean removeLike(int filmId, int userId);

    List<Film> findPopular(int limit);
}