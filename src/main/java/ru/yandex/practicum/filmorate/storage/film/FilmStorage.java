package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    Optional<Film> getById(int id);

    List<Film> getAll();

    boolean deleteById(int id);

    List<Film> getCommonFilmsSortedByPopularity(int userId, int friendId);

    List<Film> findByDirectorSorted(int directorId, String sortBy);

    List<Film> searchFilmsByTitleAndDirector(String query, boolean byTitle, boolean byDirector);
}

