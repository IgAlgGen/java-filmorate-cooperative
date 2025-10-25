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
@Qualifier("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final DirectorRowMapper directorRowMapper;

    @Override
    @Transactional
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource param = new BeanPropertySqlParameterSource(director);
        namedParameterJdbcTemplate.update(DirectorQuery.INSERT.getSql(), param, keyHolder,
                new String[]{"id"});
        Number key = keyHolder.getKey();
        director.setId(Objects.requireNonNull(key).intValue());
        return director;
    }

    @Override
    @Transactional
    public Director update(Director director) {
        SqlParameterSource param = new BeanPropertySqlParameterSource(director);
        int updated = namedParameterJdbcTemplate.update(DirectorQuery.UPDATE.getSql(), param);
        if (updated == 0) throw new NotFoundException("Режиссер на найден: " + director.getId());
        return director;
    }

    @Override
    public Optional<Director> getById(int id) {
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(DirectorQuery.SELECT_BY_ID.getSql(),
                    new MapSqlParameterSource("id", id), directorRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getAll() {
        return namedParameterJdbcTemplate.query(DirectorQuery.SELECT_ALL.getSql(), directorRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        return namedParameterJdbcTemplate.update(DirectorQuery.DELETE_BY_ID.getSql(), new MapSqlParameterSource("id", id)) > 0;
    }

    @Override
    @Transactional
    public void setDirectorsForFilm(int filmId, Set<Director> directors) {
        // очистить старые
        namedParameterJdbcTemplate.update("DELETE FROM film_directors WHERE film_id = :filmId",
                new MapSqlParameterSource("filmId", filmId));
        if (directors == null || directors.isEmpty()) return;

        // вставить новые
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES " +
                directors.stream()
                        .map(d -> "(:filmId, :directorId" + d.getId() + ")")
                        .collect(Collectors.joining(","));

        MapSqlParameterSource params = new MapSqlParameterSource("filmId", filmId);
        for (Director d : directors) params.addValue("directorId" + d.getId(), d.getId());
        namedParameterJdbcTemplate.update(sql, params);
    }

    @Override
    public Set<Director> getDirectorsByFilmId(int filmId) {
        return new HashSet<>(namedParameterJdbcTemplate.query(DirectorQuery.SELECT_BY_FILM_ID.getSql(),
                new MapSqlParameterSource("filmId", filmId), directorRowMapper));
    }
}
