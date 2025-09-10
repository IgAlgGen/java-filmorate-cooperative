package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("filmLikeDbStorage")
    private final FilmLikeStorage likeStorage;

    public Film create(Film f) {
        return filmStorage.create(f);
    }

    public Film update(Film f) {
        return filmStorage.update(f);
    }

    public Film get(int id) {
        return filmStorage.findById(id).orElseThrow();
    }

    public List<Film> getAll() {
        return filmStorage.findAll();
    }

    public void delete(int id) {
        filmStorage.deleteById(id);
    }

    public boolean exists(int id) {
        return filmStorage.existsById(id);
    }

    public void addLike(int filmId, int userId) {
        likeStorage.addLike((int) filmId, (int) userId);
    }

    public void removeLike(int filmId, int userId) {
        likeStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopular(int count) {
        return likeStorage.findPopular(count);
    }
}
