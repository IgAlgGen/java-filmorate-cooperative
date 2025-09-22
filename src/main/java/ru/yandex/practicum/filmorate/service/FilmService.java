package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;

    @Qualifier("filmLikeDbStorage")
    private final FilmLikeStorage likeStorage;

    @Qualifier("genreDbStorage")
    private final GenreStorage genreStorage;

    public Film create(Film f) {
        log.debug("Создание фильма: name='{}', releaseDate={}, duration={}", f.getName(), f.getReleaseDate(), f.getDuration());
        Film savedFilm = filmStorage.create(f);
        log.debug("Обновление жанров для фильма с ID {}: {}", f.getId(), f.getGenres());
        genreStorage.renewGenres(f.getId(), f.getGenres());
        log.debug("Жанры для фильма с ID {} обновлены: {}", f.getId(), f.getGenres());
        log.debug("Фильм создан: id={}, title='{}'", savedFilm.getId(), savedFilm.getName());
        return savedFilm;
    }

    public Film update(Film f) {
        log.debug("Обновление фильма: id={}, name='{}'", f.getId(), f.getName());
        Film updatedFilm = filmStorage.update(f);
        log.debug("Фильм обновлен: id={}, title='{}'", updatedFilm.getId(), updatedFilm.getName());
        log.debug("Обновление жанров для фильма с ID {}: {}", f.getId(), f.getGenres());
        genreStorage.renewGenres(f.getId(), f.getGenres());
        log.debug("Жанры для фильма с ID {} обновлены: {}", f.getId(), f.getGenres());
        return updatedFilm;
    }

    public Film getById(int id) {
        log.debug("Поиск фильма по ID {}", id);
        Film film = filmStorage.findById(id).orElseThrow();
        log.debug("Найден фильм: id={}, title='{}'", film.getId(), film.getName());
        Set<Genre> genreSet = genreStorage.findGeneresByFilmId(id);
        film.setGenres(genreSet);
        return film;
    }

    public List<Film> getAll() {
        log.debug("Поиск всех фильмов");
        List<Film> list = filmStorage.findAll();
        log.debug("Найдено {} фильмов", list.size());
        return list;
    }

    public void delete(int id) {
        log.debug("Удаление фильма с ID {}", id);
        filmStorage.deleteById(id);
        log.debug("Фильм с ID {} удален", id);
    }

    public boolean exists(int id) {
        return filmStorage.existsById(id);
    }

    public void addLike(int filmId, int userId) {
        log.debug("Пользователь с ID {} ставит лайк фильму с ID {}", userId, filmId);
        likeStorage.addLike((int) filmId, (int) userId);
        log.debug("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
    }

    public void removeLike(int filmId, int userId) {
        log.debug("Пользователь с ID {} удаляет лайк у фильма с ID {}", userId, filmId);
        likeStorage.removeLike(filmId, userId);
        log.debug("Пользователь с ID {} удалил лайк у фильма с ID {}", userId, filmId);
    }

    public List<Film> getPopular(int count) {
        log.debug("Получение списка из {} популярных фильмов", count);
        List<Film> popularFilms = likeStorage.findPopular(count);
        log.debug("Найдено {} популярных фильмов", popularFilms.size());
        return popularFilms;

    }
}
