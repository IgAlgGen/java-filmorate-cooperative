package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    private final String pID = "id";
    private final String pNAME = "name";
    private final String pDESCRIPTION = "description";
    private final String pRELEASEDATE = "releaseDate";
    private final String pDURATION = "duration";
    private final String pMPA = "mpa";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        final String SQL_FILM_INSERT = """
                INSERT INTO films (name, description, release_date, duration, mpa)
                VALUES (%s, %s, %s, %s, %s)
                """.formatted(par(pNAME), par(pDESCRIPTION), par(pRELEASEDATE), par(pDURATION), par(pMPA));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pNAME, film.getName())
                .addValue(pDESCRIPTION, film.getDescription())
                .addValue(pRELEASEDATE, film.getReleaseDate())
                .addValue(pDURATION, film.getDuration())
                .addValue(pMPA, film.getMpa().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(SQL_FILM_INSERT, params, keyHolder, new String[]{pID});
        Number key = keyHolder.getKey();
        film.setId(Objects.requireNonNull(key).intValue());
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        final String SQL_FILM_UPDATE = """
                UPDATE films
                SET name = %s, description = %s, release_date = %s, duration = %s, mpa = %s
                WHERE id = %s
                """.formatted(par(pNAME), par(pDESCRIPTION), par(pRELEASEDATE), par(pDURATION), par(pMPA), par(pID));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pNAME, film.getName())
                .addValue(pDESCRIPTION, film.getDescription())
                .addValue(pRELEASEDATE, film.getReleaseDate())
                .addValue(pDURATION, film.getDuration())
                .addValue(pMPA, film.getMpa().getId())
                .addValue(pID, film.getId());
        int updated = namedParameterJdbcTemplate.update(SQL_FILM_UPDATE, params);
        if (updated == 0) {
            throw new NoSuchElementException("Film not found: id=" + film.getId());
        }
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        final String SQL_FILM_SELECT_BY_ID = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                WHERE f.id = %s
                """.formatted(par(pID));
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(SQL_FILM_SELECT_BY_ID,
                    new MapSqlParameterSource(pID, id), filmRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        final String SQL_FILM_SELECT_ALL = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                ORDER BY f.id
                """;
        return namedParameterJdbcTemplate.query(SQL_FILM_SELECT_ALL, filmRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        final String SQL_FILM_DELETE = """
                DELETE FROM films WHERE id = %s
                """.formatted(par(pID));
        return namedParameterJdbcTemplate.update(SQL_FILM_DELETE, new MapSqlParameterSource(pID, id)) > 0;
    }
}
