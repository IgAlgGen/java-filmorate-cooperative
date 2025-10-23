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

    /**
     * Получение списка фильмов по идентификатору режиссера с сортировкой
     * @param directorId id режиссера
     * @param sortBy параметр сортировки: "year" - по году выпуска, "likes" - по количеству лайков
     * @return список фильмов
     */
    @Override
    public List<Film> findByDirectorSorted(int directorId, String sortBy) {
        String orderBy = switch (sortBy) {
            case "likes" -> "likes_count DESC, f.id";
            case "year"  -> "EXTRACT(YEAR FROM f.release_date), f.id";
            default      -> "f.id";
        };

        final String sql = """
        SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
             , COALESCE(lc.cnt, 0) AS likes_count
        FROM films f
        JOIN film_directors fd ON fd.film_id = f.id
        LEFT JOIN (
           SELECT film_id, COUNT(*) AS cnt
           FROM film_likes
           GROUP BY film_id
        ) lc ON lc.film_id = f.id
        WHERE fd.director_id = :directorId
        ORDER BY %s
        """.formatted(orderBy);

        List<Film> films = namedParameterJdbcTemplate.query(
                sql, new MapSqlParameterSource("directorId", directorId), filmRowMapper);
        return films;
    }

    @Override
    public List<Film> searchFilmsByTitleAndDirector(String query, boolean byTitle, boolean byDirector) {
        String queryLower = "%" + query.trim().toLowerCase() + "%";
        StringBuilder where = new StringBuilder();
        where.append("(");
        if (byTitle) {
            where.append("LOWER(f.name) LIKE :pattern");
        }
        if (byDirector) {
            if (byTitle) where.append(" OR ");
            where.append("EXISTS (")
                    .append(" SELECT 1 FROM film_directors fd")
                    .append(" JOIN directors d ON d.id = fd.director_id")
                    .append(" WHERE fd.film_id = f.id")
                    .append(" AND LOWER(d.name) LIKE :pattern")
                    .append(")");
        }
        where.append(")");

        String sql = """
        SELECT f.id, f.name, f.release_date, f.description, f.duration, f.mpa
        FROM films f
        LEFT JOIN (
            SELECT fl.film_id, COUNT(fl.user_id) AS likes_cnt
            FROM film_likes fl
            GROUP BY fl.film_id
        ) lc ON lc.film_id = f.id
        WHERE %s
        ORDER BY COALESCE(lc.likes_cnt, 0) DESC, f.name ASC, f.id ASC
        """.formatted(where.toString());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("pattern", queryLower);

        return namedParameterJdbcTemplate.query(sql, params, filmRowMapper);
    }
}
