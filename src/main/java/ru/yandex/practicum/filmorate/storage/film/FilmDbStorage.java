package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.*;

import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.Film.*;

@Repository
@RequiredArgsConstructor
@org.springframework.beans.factory.annotation.Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper = new FilmRowMapper();

    @Override
    @Transactional
    public Film create(Film film) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("films")
                .usingGeneratedKeyColumns("film_id");
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
                SQL_UPDATE,
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
    public Optional<Film> findById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(SQL_SELECT_BY_ID, mapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

//    @Override
//    public List<Film> findAll() {
//        return jdbcTemplate.query(SQL_SELECT_ALL, mapper);
//    }

    private static final String SQL_SELECT_ALL_WITH_GENRES = """
SELECT
        f.film_id       AS f_id,
        f.name          AS f_name,
        f.description   AS f_desc,
        f.release_date  AS f_release_date,
        f.duration      AS f_duration,
        m.id            AS mpa_id,
        m.name          AS mpa_name,
        g.id            AS g_id,
        g.name          AS g_name
    FROM films f
    JOIN mpa_ratings m ON m.id = f.mpa
    LEFT JOIN film_genres fg ON fg.film_id = f.film_id
    LEFT JOIN genres g       ON g.id = fg.genre_id
    ORDER BY f.film_id, g.id
    """;

    @Override
    public List<Film> findAll() {
        return jdbcTemplate.query(SQL_SELECT_ALL_WITH_GENRES, rs -> {
            Map<Integer, Film> acc = new LinkedHashMap<>();
            while (rs.next()) {
                int filmId = rs.getInt("f_id");
                Film film = acc.get(filmId);
                if (film == null) {
                    film = new Film();
                    film.setId(filmId);
                    film.setName(rs.getString("f_name"));
                    film.setDescription(rs.getString("f_desc"));
                    film.setReleaseDate(rs.getDate("f_release_date").toLocalDate());
                    film.setDuration(rs.getInt("f_duration"));
                    String mpaCode = rs.getString("mpa_id");
                    if (mpaCode != null) {
                        film.setMpa(MpaRating.fromId(Integer.parseInt(mpaCode)));
                    }
                    film.setGenres(new LinkedHashSet<>()); // важно
                    acc.put(filmId, film);
                }

                int gId = rs.getInt("g_id");
                if (!rs.wasNull()) {
                    film.getGenres().add(new Genre(gId, rs.getString("g_name")));
                }
            }
            return new ArrayList<>(acc.values());
        });
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        return jdbcTemplate.update(SQL_DELETE, id) > 0;
    }

    @Override
    public boolean existsById(int id) {
        Boolean exists = jdbcTemplate.queryForObject(SQL_FILM_EXISTS, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }
}
