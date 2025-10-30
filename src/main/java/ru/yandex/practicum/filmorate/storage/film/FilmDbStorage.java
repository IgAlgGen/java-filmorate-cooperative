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

    @Override
    @Transactional
    public Film create(Film film) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpa", film.getMpa().getId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(FilmQuery.INSERT.getSql(), params, keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        film.setId(Objects.requireNonNull(key).intValue());
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("mpa", film.getMpa().getId())
                .addValue("id", film.getId());
        int updated = namedParameterJdbcTemplate.update(FilmQuery.UPDATE.getSql(), params);
        if (updated == 0) {
            throw new NoSuchElementException("Film not found: id=" + film.getId());
        }
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(FilmQuery.SELECT_BY_ID.getSql(),
                    new MapSqlParameterSource("id", id), filmRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        return namedParameterJdbcTemplate.query(FilmQuery.SELECT_ALL.getSql(), filmRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        return namedParameterJdbcTemplate.update(FilmQuery.DELETE_BY_ID.getSql(), new MapSqlParameterSource("id", id)) > 0;
    }

    @Override
    public List<Film> getCommonFilmsSortedByPopularity(int userId, int friendId) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        return namedParameterJdbcTemplate.query(FilmQuery.SELECT_COMMON_FILMS.getSql(), params, filmRowMapper);
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

        List<Film> films = namedParameterJdbcTemplate.query(FilmQuery.SELECT_BY_DIRECTOR_SORTED.getSql().formatted(orderBy),
                new MapSqlParameterSource("directorId", directorId), filmRowMapper);
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

        return namedParameterJdbcTemplate.query(FilmQuery.SELECT_BY_TITLE_AND_DIRECTOR.getSql().formatted(where.toString()),
                params, filmRowMapper);
    }
}
