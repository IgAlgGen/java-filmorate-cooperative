package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    List<Genre> findAll();
    Optional<Genre> findById(int id);
    Genre create(Genre genre);
    Genre update(Genre genre);
    boolean deleteById(int id);
    void renewGenres(int filmId, Set<Genre> genres);
    Set<Genre> findByFilmId(int filmId);
}
