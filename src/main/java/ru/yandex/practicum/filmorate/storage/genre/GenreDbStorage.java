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

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper mapper = new GenreRowMapper();

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", mapper);
    }

    @Override
    public Optional<Genre> findById(int id) {
        List<Genre> list = jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }

    @Override
    @Transactional
    public Genre create(Genre genre) {
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (?, ?)", genre.getId(), genre.getName());
        return genre;
    }

    @Override
    @Transactional
    public Genre update(Genre genre) {
        jdbcTemplate.update("UPDATE genres SET name = ? WHERE id = ?", genre.getName(), genre.getId());
        return genre;
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        int deleted = jdbcTemplate.update("DELETE FROM genres WHERE id = ?", id);
        return deleted > 0;
    }

    @Override
    public boolean existsById(int id) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM genres WHERE id = ?", Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void renewGenres(int filmId, Set<Genre> genres) {
        // стереть старые связи
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);

        if (genres == null || genres.isEmpty()) return;

        // убрать возможные дубли по id
        Set<Integer> ids = genres.stream().map(Genre::getId).collect(java.util.stream.Collectors.toSet());

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
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
        return new LinkedHashSet<>(jdbcTemplate.query(
                "SELECT g.id, g.name\n" +
                "    FROM genres g\n" +
                "    JOIN film_genres fg ON fg.genre_id = g.id\n" +
                "    WHERE fg.film_id = ?\n" +
                "    ORDER BY g.id", mapper, filmId));
    }

}
