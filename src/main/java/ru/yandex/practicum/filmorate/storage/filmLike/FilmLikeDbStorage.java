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

    private final String pFILMID = "filmId";
    private final String pUSERID = "userId";

    private static String par(String name) {
        return ":" + name;
    }

    @Override
    @Transactional
    public void addLike(int filmId, int userId) {
        final String sqlFilmlikeUpsert = """
                MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (%s, %s)
                """.formatted(par(pFILMID), par(pUSERID));
        assertFilmExists(filmId);
        assertUserExists(userId);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(pFILMID, filmId)
                .addValue(pUSERID, userId);
        namedParameterJdbcTemplate.update(sqlFilmlikeUpsert, mapSqlParameterSource);
    }

    @Override
    @Transactional
    public boolean removeLike(int filmId, int userId) {
        final String sqlFilmlikeDelete = """
                DELETE FROM film_likes WHERE film_id = %s AND user_id = %s
                """.formatted(par(pFILMID), par(pUSERID));
        assertFilmExists(filmId);
        assertUserExists(userId);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(pFILMID, filmId)
                .addValue(pUSERID, userId);
        return namedParameterJdbcTemplate.update(sqlFilmlikeDelete, mapSqlParameterSource) > 0;
    }

    @Override
    public List<Film> findPopular(int limit) {
        final String sqlFilmlikePopular = """
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
        return namedParameterJdbcTemplate.query(sqlFilmlikePopular, mapSqlParameterSource, filmRowMapper);
    }

    private void assertFilmExists(int filmId) {
        final String sqlFilmExists = """
                SELECT EXISTS(SELECT 1 FROM films WHERE id = %s)
                """.formatted(par(pFILMID));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(pFILMID, filmId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(sqlFilmExists, mapSqlParameterSource, Boolean.class);
        if (Boolean.FALSE.equals(ok)) throw new NotFoundException("Фильм не найден: id=" + filmId);
    }

    private void assertUserExists(int userId) {
        final String sqlUserExists = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = %s)
                """.formatted(par(pUSERID));
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue(pUSERID, userId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(sqlUserExists,
                mapSqlParameterSource, Boolean.class);
        if (Boolean.FALSE.equals(ok)) throw new NotFoundException("Пользователь не найден: id=" + userId);
    }
}