package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("filmLikeDbStorage")
    private final FilmLikeStorage likeStorage;

    public Film create(Film f) {
        log.debug("Создание фильма: name='{}', releaseDate={}, duration={}", f.getName(), f.getReleaseDate(), f.getDuration());
        Film savedFilm = filmStorage.create(f);
        log.info("Фильм создан: id={}, title='{}'", savedFilm.getId(), savedFilm.getName());
        return savedFilm;
    }

    public Film update(Film f) {
        log.debug("Обновление фильма: id={}, name='{}', releaseDate={}, duration={}, mpa='{}'",
                f.getId(),
                f.getName(),
                f.getReleaseDate(),
                f.getDuration(),
                f.getMpa());
        Film updatedFilm = filmStorage.update(f);
        log.info("Фильм обновлен: id={}, title='{}'", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }

    public Film get(int id) {
        return filmStorage.findById(id).orElseThrow();
    }

    public List<Film> getAll() {
        log.debug("Поиск всех фильмов");
        List<Film> list = filmStorage.findAll();
        log.debug("Найдено {} фильмов", list.size());
        return list;
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
