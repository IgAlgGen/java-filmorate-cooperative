package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    private final String pID = "id";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    public List<MpaRating> findAll() {
        final String sqlMpaSelectAll = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                ORDER BY m.id
                """;
        return namedParameterJdbcTemplate.query(sqlMpaSelectAll, mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        assertMpaExists(id);
        final String sqlMpaSelectById = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                WHERE m.id = %s
                """.formatted(par(pID));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(pID, id);
        return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sqlMpaSelectById, params, mpaRowMapper));
    }

    @Override
    public void assertMpaExists(int id) {
        final String sqlMpaExists = """
                SELECT EXISTS(SELECT 1 FROM mpa_ratings WHERE id = %s)
                """.formatted(par(pID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pID, id);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(sqlMpaExists, params, Boolean.class);
        if (exists == null || !exists) {
            throw new NotFoundException("MPA рейтинг с ID " + id + " не найден.");
        }
    }
}
