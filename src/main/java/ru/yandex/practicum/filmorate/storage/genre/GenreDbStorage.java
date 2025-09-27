package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.Genre.*;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper MAPPER = new GenreRowMapper();

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(SQL_GENRE_SELECT_ALL, MAPPER);
    }

    @Override
    public Optional<Genre> findById(int id) {
        List<Genre> list = jdbcTemplate.query(SQL_GENRE_SELECT_BY_ID, MAPPER, id);
        return list.stream().findFirst();
    }

    @Override
    @Transactional
    public Genre create(Genre genre) {
        jdbcTemplate.update(SQL_GENRE_INSERT, genre.getId(), genre.getName());
        return genre;
    }

    @Override
    @Transactional
    public Genre update(Genre genre) {
        jdbcTemplate.update(SQL_GENRE_UPDATE, genre.getName(), genre.getId());
        return genre;
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        int deleted = jdbcTemplate.update(SQL_GENRE_DELETE, id);
        return deleted > 0;
    }

    @Override
    @Transactional
    public void renewGenres(int filmId, Set<Genre> genres) {
        // стереть старые связи
        jdbcTemplate.update(SQL_GENRE_DELETE_BY_FILM_ID, filmId);

        if (genres == null || genres.isEmpty()) return;

        // убрать возможные дубли по id
        Set<Integer> ids = genres.stream().map(Genre::getId).collect(java.util.stream.Collectors.toSet());

        jdbcTemplate.batchUpdate(
                SQL_GENRE_INSERT_BY_FILM_ID,
                ids,
                ids.size(),
                (ps, genreId) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genreId);
                }
        );
    }

    @Override
    public Set<Genre> findByFilmId(int filmId) {
        return new LinkedHashSet<>(jdbcTemplate.query(SQL_GENRE_SELECT_BY_FILM_ID, MAPPER, filmId));
    }

}
