package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

import static java.util.stream.Collectors.toSet;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    private final String P_GENRE_ID = "id";
    private final String P_GENRE_NAME = "name";
    private final String P_FILM_ID = "filmId";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    public List<Genre> findAll() {
        final String SQL_GENRE_SELECT_ALL = """
                SELECT g.id, g.name
                FROM genres g
                ORDER BY g.id
                """;
        return namedParameterJdbcTemplate.query(SQL_GENRE_SELECT_ALL, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(int genreId) {
        assertGenreExists(genreId);
        final String SQL_GENRE_SELECT_BY_ID = """
                SELECT g.id, g.name
                FROM genres g
                WHERE g.id = %s
                """.formatted(par(P_GENRE_ID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_GENRE_ID, genreId);
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(SQL_GENRE_SELECT_BY_ID, params, genreRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Genre create(Genre genre) {
        final String SQL_GENRE_INSERT = """
                INSERT INTO genres (id, name) VALUES (%s, %s)
                """.formatted(par(P_GENRE_ID), par(P_GENRE_NAME));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(P_GENRE_ID, genre.getId())
                .addValue(P_GENRE_NAME, genre.getName());
        namedParameterJdbcTemplate.update(SQL_GENRE_INSERT, params);
        return genre;
    }

    @Override
    @Transactional
    public Genre update(Genre genre) {
        assertGenreExists(genre.getId());
        final String SQL_GENRE_UPDATE = """
                UPDATE genres SET name = :name WHERE id = %s
                """.formatted(par(P_GENRE_ID));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(P_GENRE_ID, genre.getId())
                .addValue(P_GENRE_NAME, genre.getName());
        namedParameterJdbcTemplate.update(SQL_GENRE_UPDATE, params);
        return genre;
    }

    @Override
    @Transactional
    public boolean deleteById(int genreId) {
        final String SQL_GENRE_DELETE = """
                DELETE FROM genres WHERE id = %s
                """.formatted(par(P_GENRE_ID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_GENRE_ID, genreId);
        int deleted = namedParameterJdbcTemplate.update(SQL_GENRE_DELETE, params);
        return deleted > 0;
    }

    @Override
    @Transactional
    public void renewGenres(int filmId, Set<Genre> genres) {
        final String SQL_GENRE_DELETE_BY_FILM_ID = """
                DELETE FROM film_genres WHERE film_id = %s
                """.formatted(par(P_FILM_ID));
        final String SQL_GENRE_INSERT_BY_FILM_ID = """
                INSERT INTO film_genres (film_id, genre_id) VALUES (%s, %s)
                """.formatted(par(P_FILM_ID), par(P_GENRE_ID));
        // стереть старые связи
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_FILM_ID, filmId);
        namedParameterJdbcTemplate.update(SQL_GENRE_DELETE_BY_FILM_ID, params);
        // добавить новые связи
        if (genres == null || genres.isEmpty()) return;
        // убрать возможные дубли по id
        Set<Integer> ids = genres.stream().map(Genre::getId).collect(toSet());
        SqlParameterSource[] batch =
                ids.stream()
                        .map(genreId -> new MapSqlParameterSource()
                                .addValue(P_FILM_ID, filmId)
                                .addValue(P_GENRE_ID, genreId))
                        .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(SQL_GENRE_INSERT_BY_FILM_ID, batch);
    }

    @Override
    public Set<Genre> findByFilmId(int filmId) {
        final String SQL_GENRE_SELECT_BY_FILM_ID = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = %s
                ORDER BY g.id
                """.formatted(par(P_FILM_ID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_FILM_ID, filmId);
        return new LinkedHashSet<>(namedParameterJdbcTemplate.query(SQL_GENRE_SELECT_BY_FILM_ID, params, genreRowMapper));
    }

    @Override
    public void assertGenresExists(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;
        // собрать уникальные id и отфильтровать
        Set<Integer> ids = genres.stream()
                .map(Genre::getId)
                .filter(id -> id != null && id > 0)
                .collect(toSet());
        if (ids.isEmpty()) return;
        final String SQL_SELECT_EXISTING_IDS =
                "SELECT id FROM genres WHERE id IN (:ids)";
        List<Integer> found = namedParameterJdbcTemplate.queryForList(
                SQL_SELECT_EXISTING_IDS,
                Map.of("ids", ids),
                Integer.class
        );
        // вычислить, кого не нашли
        Set<Integer> missing = new HashSet<>(ids);
        missing.removeAll(new HashSet<>(found));
        if (!missing.isEmpty()) {
            throw new NotFoundException("Жанры с ID " + missing + " не найдены.");
        }
    }

    @Override
    public void assertGenreExists(int id) {
    String SQL_GENRE_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM genres WHERE id = %s)
                """.formatted(par(P_GENRE_ID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_GENRE_ID, id);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(SQL_GENRE_EXISTS, params, Boolean.class);
        if (exists == null || !exists) {
            throw new NotFoundException("Жанр с ID " + id + " не найден.");
        }
    }
}
