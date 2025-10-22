package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Director create(Director director);

    Director update(Director director);

    Optional<Director> getById(int id);

    List<Director> getAll();

    boolean deleteById(int id);

    void setDirectorsForFilm(int filmId, Set<Director> directors);

    Set<Director> getDirectorsByFilmId(int filmId);
}
