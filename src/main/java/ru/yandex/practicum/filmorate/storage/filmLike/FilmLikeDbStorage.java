package ru.yandex.practicum.filmorate.storage.filmLike;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.util.List;

import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.FilmLike.*;
import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.Film.SQL_FILM_EXISTS;
import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.User.SQL_USER_EXISTS;

@Repository
@RequiredArgsConstructor
@Qualifier("filmLikeDbStorage")
public class FilmLikeDbStorage implements FilmLikeStorage {

    private final JdbcTemplate jdbc;

    private final FilmRowMapper filmRowMapper = new FilmRowMapper();

    @Override
    @Transactional
    public void addLike(int filmId, int userId) {
        assertFilmExists(filmId);
        assertUserExists(userId);
        jdbc.update(SQL_FILMLIKE_UPSERT, filmId, userId);
    }

    @Override
    @Transactional
    public void removeLike(int filmId, int userId) {
        assertFilmExists(filmId);
        assertUserExists(userId);
        jdbc.update(SQL_FILMLIKE_DELETE, filmId, userId);
    }

    @Override
    public List<Film> findPopular(int limit) {
        return jdbc.query(SQL_FILMLIKE_POPULAR, filmRowMapper, limit);
    }

    private void assertFilmExists(int filmId) {
        Boolean ok = jdbc.queryForObject(SQL_FILM_EXISTS, Boolean.class, filmId);
        if (!ok) throw new NotFoundException("Фильм не найден: id=" + filmId);
    }

    private void assertUserExists(int userId) {
        Boolean ok = jdbc.queryForObject(SQL_USER_EXISTS, Boolean.class, userId);
        if (!ok) throw new NotFoundException("Пользователь не найден: id=" + userId);
    }
}