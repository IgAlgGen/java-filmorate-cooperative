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

    private final String pGENREID = "id";
    private final String pGENRENAME = "name";
    private final String pFILMID = "filmId";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    public List<Genre> findAll() {
        final String sqlGenreSelectAll = """
                SELECT g.id, g.name
                FROM genres g
                ORDER BY g.id
                """;
        return namedParameterJdbcTemplate.query(sqlGenreSelectAll, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(int genreId) {
        assertGenreExists(genreId);
        final String sqlGenreSelectById = """
                SELECT g.id, g.name
                FROM genres g
                WHERE g.id = %s
                """.formatted(par(pGENREID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pGENREID, genreId);
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sqlGenreSelectById, params, genreRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Genre create(Genre genre) {
        final String sqlGenreInsert = """
                INSERT INTO genres (id, name) VALUES (%s, %s)
                """.formatted(par(pGENREID), par(pGENRENAME));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pGENREID, genre.getId())
                .addValue(pGENRENAME, genre.getName());
        namedParameterJdbcTemplate.update(sqlGenreInsert, params);
        return genre;
    }

    @Override
    @Transactional
    public Genre update(Genre genre) {
        assertGenreExists(genre.getId());
        final String sqlGenreUpdate = """
                UPDATE genres SET name = :name WHERE id = %s
                """.formatted(par(pGENREID));
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pGENREID, genre.getId())
                .addValue(pGENRENAME, genre.getName());
        namedParameterJdbcTemplate.update(sqlGenreUpdate, params);
        return genre;
    }

    @Override
    @Transactional
    public boolean deleteById(int genreId) {
        final String sqlGenreDelete = """
                DELETE FROM genres WHERE id = %s
                """.formatted(par(pGENREID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pGENREID, genreId);
        int deleted = namedParameterJdbcTemplate.update(sqlGenreDelete, params);
        return deleted > 0;
    }

    @Override
    @Transactional
    public void renewGenres(int filmId, Set<Genre> genres) {
        final String sqlGenreDeleteByFilmId = """
                DELETE FROM film_genres WHERE film_id = %s
                """.formatted(par(pFILMID));
        final String sqlGenreInsertByFilmId = """
                INSERT INTO film_genres (film_id, genre_id) VALUES (%s, %s)
                """.formatted(par(pFILMID), par(pGENREID));
        // стереть старые связи
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pFILMID, filmId);
        namedParameterJdbcTemplate.update(sqlGenreDeleteByFilmId, params);
        // добавить новые связи
        if (genres == null || genres.isEmpty()) return;
        // убрать возможные дубли по id
        Set<Integer> ids = genres.stream().map(Genre::getId).collect(toSet());
        SqlParameterSource[] batch =
                ids.stream()
                        .map(genreId -> new MapSqlParameterSource()
                                .addValue(pFILMID, filmId)
                                .addValue(pGENREID, genreId))
                        .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(sqlGenreInsertByFilmId, batch);
    }

    @Override
    public Set<Genre> findByFilmId(int filmId) {
        final String sqlGenreSelectByFilmId = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON fg.genre_id = g.id
                WHERE fg.film_id = %s
                ORDER BY g.id
                """.formatted(par(pFILMID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pFILMID, filmId);
        return new LinkedHashSet<>(namedParameterJdbcTemplate.query(sqlGenreSelectByFilmId, params, genreRowMapper));
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
        final String sqlSelectExistingIds =
                "SELECT id FROM genres WHERE id IN (:ids)";
        List<Integer> found = namedParameterJdbcTemplate.queryForList(
                sqlSelectExistingIds,
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
    String sqlGenreExists = """
                SELECT EXISTS(SELECT 1 FROM genres WHERE id = %s)
                """.formatted(par(pGENREID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pGENREID, id);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(sqlGenreExists, params, Boolean.class);
        if (exists == null || !exists) {
            throw new NotFoundException("Жанр с ID " + id + " не найден.");
        }
    }
}
