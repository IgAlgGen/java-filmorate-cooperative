package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.Film.*;

@Repository
@RequiredArgsConstructor
@org.springframework.beans.factory.annotation.Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final FilmRowMapper MAPPER = new FilmRowMapper();

    @Override
    @Transactional
    public Film create(Film film) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("id");
        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("mpa", film.getMpa() != null ? film.getMpa().getId() : 1);
        params.put("genres", film.getGenres());
        Number key = insert.executeAndReturnKey(params);
        film.setId(key.intValue());
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        int updated = jdbcTemplate.update(
                SQL_FILM_UPDATE,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : 1,
                film.getId()
        );
        if (updated == 0) {
            throw new NoSuchElementException("Film not found: id=" + film.getId());
        }
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(SQL_FILM_SELECT_BY_ID, MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        return jdbcTemplate.query(SQL_FILM_SELECT_ALL, MAPPER);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        return jdbcTemplate.update(SQL_FILM_DELETE, id) > 0;
    }
}
