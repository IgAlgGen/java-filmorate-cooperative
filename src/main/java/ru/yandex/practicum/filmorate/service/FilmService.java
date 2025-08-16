package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final InMemoryStorage<Film> filmStorage;
    private final InMemoryStorage<User> userStorage;

    public Film getById(int id) {
        return filmStorage.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Фильм с id=%d не найден", id)));
    }

    public List<Film> getAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(int id, Film film) {
        return filmStorage.update(id, film).orElseThrow(() ->
                new NotFoundException(String.format("Фильм с id=%d не найден", id)));
    }

    // Лайки

    public void addLike(int filmId, int userId) {
        // убеждаемся, что фильм и пользователь существуют
        Film film = getById(filmId);
        userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id=%d не найден", userId)));
        film.addLike(userId);
    }

    public void removeLike(int filmId, int userId) {
        Film film = getById(filmId);
        film.removeLike(userId);
    }

    public List<Film> getPopular(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::likeCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
