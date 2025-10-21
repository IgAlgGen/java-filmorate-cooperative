package ru.yandex.practicum.filmorate.storage.filmLike;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface FilmLikeStorage {
    void addLike(int filmId, int userId);

    boolean removeLike(int filmId, int userId);

    List<Film> findPopular(int limit, Long genreId, Integer year);

    List<Film> findPopular(int limit);

    Map<Integer, Set<Integer>> getUsersLikesData();
}
