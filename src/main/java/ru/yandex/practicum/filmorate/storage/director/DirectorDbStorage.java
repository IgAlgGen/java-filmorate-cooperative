package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    @Qualifier("namedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    private static final String pID = "id";
    private static final String pNAME = "name";
    private static final String pFILMID = "filmId";
    private static final String pDIRID = "directorId";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    @Transactional
    public Director create(Director director) {
        final String sqlDirectorInsert = "INSERT INTO directors (name) VALUES (" + par(pNAME) + ")";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource param = new BeanPropertySqlParameterSource(director);
        namedParameterJdbcTemplate.update(sqlDirectorInsert, param, keyHolder,
                new String[]{pID});
        Number key = keyHolder.getKey();
        director.setId(Objects.requireNonNull(key).intValue());
        return director;
    }

    @Override
    @Transactional
    public Director update(Director director) {
        final String sqlDirectorUpdate = "UPDATE directors SET name=" + par(pNAME) + " WHERE id=" + par(pID);
        SqlParameterSource param = new BeanPropertySqlParameterSource(director);
        int updated = namedParameterJdbcTemplate.update(sqlDirectorUpdate, param);
        if (updated == 0) throw new NotFoundException("Режиссер на найден: " + director.getId());
        return director;
    }

    @Override
    public Optional<Director> getById(int id) {
        final String sqlDirectorSelectById = "SELECT id, name FROM directors WHERE id=" + par(pID);
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sqlDirectorSelectById, new MapSqlParameterSource(pID, id), directorRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getAll() {
        final String sqlDirectorSelectAll = "SELECT id, name FROM directors ORDER BY id";
        return namedParameterJdbcTemplate.query(sqlDirectorSelectAll, directorRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        final String sqlDirectorDeleteById = "DELETE FROM directors WHERE id=" + par(pID);
        return namedParameterJdbcTemplate.update(sqlDirectorDeleteById, new MapSqlParameterSource(pID, id)) > 0;
    }

    @Override
    @Transactional
    public void setDirectorsForFilm(int filmId, Set<Director> directors) {
        // очистить старые
        namedParameterJdbcTemplate.update("DELETE FROM film_directors WHERE film_id=" + par(pFILMID),
                new MapSqlParameterSource(pFILMID, filmId));
        if (directors == null || directors.isEmpty()) return;

        // вставить новые
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES " +
                directors.stream()
                        .map(d -> "(" + par(pFILMID) + ", " + (par(pDIRID) + d.getId()) + ")")
                        .collect(Collectors.joining(","));

        MapSqlParameterSource params = new MapSqlParameterSource(pFILMID, filmId);
        for (Director d : directors) params.addValue(pDIRID + d.getId(), d.getId());
        namedParameterJdbcTemplate.update(sql, params);
        /* вставить новые - вариант с batchUpdate (оставлю тут, может пригодится)
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (:filmId, :directorId)";
        List<MapSqlParameterSource> batch = directors.stream()
            .map(d -> new MapSqlParameterSource()
            .addValue("filmId", filmId)
            .addValue("directorId", d.getId()))
            .toList();
        namedParameterJdbcTemplate.batchUpdate(sql, batch.toArray(MapSqlParameterSource[]::new));
         */
    }

    @Override
    public Set<Director> getDirectorsByFilmId(int filmId) {
        final String sql = """
                SELECT d.id, d.name
                FROM directors d
                JOIN film_directors fd ON fd.director_id = d.id
                WHERE fd.film_id = :%s
                ORDER BY d.id
                """.formatted(par(pFILMID));
        return new HashSet<>(namedParameterJdbcTemplate.query(sql, new MapSqlParameterSource(pFILMID, filmId), directorRowMapper));
    }
}
