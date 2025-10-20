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

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

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
        final String sqlFilmInsert = """
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
        namedParameterJdbcTemplate.update(sqlFilmInsert, params, keyHolder, new String[]{pID});
        Number key = keyHolder.getKey();
        film.setId(Objects.requireNonNull(key).intValue());
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        final String sqlFilmUpdate = """
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
        int updated = namedParameterJdbcTemplate.update(sqlFilmUpdate, params);
        if (updated == 0) {
            throw new NoSuchElementException("Film not found: id=" + film.getId());
        }
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        final String sqlFilmSelectById = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                WHERE f.id = %s
                """.formatted(par(pID));
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sqlFilmSelectById,
                    new MapSqlParameterSource(pID, id), filmRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        final String sqlFilmSelectAll = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films f
                ORDER BY f.id
                """;
        return namedParameterJdbcTemplate.query(sqlFilmSelectAll, filmRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        final String sqlFilmDelete = """
                DELETE FROM films WHERE id = %s
                """.formatted(par(pID));
        return namedParameterJdbcTemplate.update(sqlFilmDelete, new MapSqlParameterSource(pID, id)) > 0;
    }

    @Override
    public List<Film> getCommonFilmsSortedByPopularity(int userId, int friendId) {
        final String sqlGetCommonFilms = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
                FROM films AS f
                JOIN film_likes AS fl ON f.id = fl.film_id
                WHERE f.id IN (
                    SELECT film_id FROM film_likes WHERE user_id = :userId
                )
                AND f.id IN (
                    SELECT film_id FROM film_likes WHERE user_id = :friendId
                )
                GROUP BY f.id
                ORDER BY COUNT(fl.user_id) DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return namedParameterJdbcTemplate.query(sqlGetCommonFilms, params, filmRowMapper);
    }

}
