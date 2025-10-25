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

    @Override
    public List<Genre> findAll() {
        return namedParameterJdbcTemplate.query(GenreQuery.SELECT_ALL.getSql(), genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(int genreId) {
        assertGenreExists(genreId);
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", genreId);
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(GenreQuery.SELECT_BY_ID.getSql(), params, genreRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public Genre create(Genre genre) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", genre.getId())
                .addValue("name", genre.getName());
        namedParameterJdbcTemplate.update(GenreQuery.INSERT.getSql(), params);
        return genre;
    }

    @Override
    @Transactional
    public Genre update(Genre genre) {
        assertGenreExists(genre.getId());
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", genre.getId())
                .addValue("name", genre.getName());
        namedParameterJdbcTemplate.update(GenreQuery.UPDATE.getSql(), params);
        return genre;
    }

    @Override
    @Transactional
    public boolean deleteById(int genreId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", genreId);
        int deleted = namedParameterJdbcTemplate.update(GenreQuery.DELETE_BY_ID.getSql(), params);
        return deleted > 0;
    }

    @Override
    @Transactional
    public void renewGenres(int filmId, Set<Genre> genres) {
        // стереть старые связи
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("filmId", filmId);
        namedParameterJdbcTemplate.update(GenreQuery.SELECT_BY_FILM_ID.getSql(), params);

        // добавить новые связи
        if (genres == null || genres.isEmpty()) return;
        // убрать возможные дубли по id
        List<Integer> ids = genres.stream()
                .map(Genre::getId)
                .filter(Objects::nonNull)
                .sorted()
                .toList();
        SqlParameterSource[] batch = ids.stream()
                .map(gid -> new MapSqlParameterSource().addValue("filmId", filmId).addValue("id", gid))
                .toArray(SqlParameterSource[]::new);
        namedParameterJdbcTemplate.batchUpdate(GenreQuery.INSERT_BY_FILM_ID.getSql(), batch);
    }

    @Override
    public Set<Genre> findByFilmId(int filmId) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("filmId", filmId);
        return new LinkedHashSet<>(namedParameterJdbcTemplate.query(GenreQuery.SELECT_BY_FILM_ID.getSql(), params, genreRowMapper));
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
                SELECT EXISTS(SELECT 1 FROM genres WHERE id = :id)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(sqlGenreExists, params, Boolean.class);
        if (exists == null || !exists) {
            throw new NotFoundException("Жанр с ID " + id + " не найден.");
        }
    }
}
