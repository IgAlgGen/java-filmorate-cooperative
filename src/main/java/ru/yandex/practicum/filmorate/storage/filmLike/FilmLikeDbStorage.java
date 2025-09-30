package ru.yandex.practicum.filmorate.storage.filmLike;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Qualifier("filmLikeDbStorage")
public class FilmLikeDbStorage implements FilmLikeStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private final String P_FILM_ID = "filmId";
    private final String P_USER_ID = "userId";

    private static String par(String name) {
        return ":" + name;
    }

    @Override
    @Transactional
    public void addLike(int filmId, int userId) {
        final String SQL_FILMLIKE_UPSERT = """
                MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (%s, %s)
                """.formatted(par(P_FILM_ID), par(P_USER_ID));
        assertFilmExists(filmId);
        assertUserExists(userId);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(P_FILM_ID, filmId)
                .addValue(P_USER_ID, userId);
        namedParameterJdbcTemplate.update(SQL_FILMLIKE_UPSERT, mapSqlParameterSource);
    }

    @Override
    @Transactional
    public boolean removeLike(int filmId, int userId) {
        final String SQL_FILMLIKE_DELETE = """
                DELETE FROM film_likes WHERE film_id = %s AND user_id = %s
                """.formatted(par(P_FILM_ID), par(P_USER_ID));
        assertFilmExists(filmId);
        assertUserExists(userId);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(P_FILM_ID, filmId)
                .addValue(P_USER_ID, userId);
        return namedParameterJdbcTemplate.update(SQL_FILMLIKE_DELETE, mapSqlParameterSource) > 0;
    }

    @Override
    public List<Film> findPopular(int limit) {
        final String SQL_FILMLIKE_POPULAR = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                LEFT JOIN film_likes fl ON fl.film_id = f.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                ORDER BY COUNT(fl.user_id) DESC, f.id ASC
                LIMIT :limit
                """;
        if (limit <= 0) return Collections.emptyList();
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("limit", limit);
        return namedParameterJdbcTemplate.query(SQL_FILMLIKE_POPULAR, mapSqlParameterSource, filmRowMapper);
    }

    private void assertFilmExists(int filmId) {
        final String SQL_FILM_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM films WHERE id = %s)
                """.formatted(par(P_FILM_ID));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(P_FILM_ID, filmId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(SQL_FILM_EXISTS, mapSqlParameterSource, Boolean.class);
        if (Boolean.FALSE.equals(ok)) throw new NotFoundException("Фильм не найден: id=" + filmId);
    }

    private void assertUserExists(int userId) {
        final String SQL_USER_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = %s)
                """.formatted(par(P_USER_ID));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(P_USER_ID, userId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(SQL_USER_EXISTS,
                mapSqlParameterSource, Boolean.class);
        if (Boolean.FALSE.equals(ok)) throw new NotFoundException("Пользователь не найден: id=" + userId);
    }
}