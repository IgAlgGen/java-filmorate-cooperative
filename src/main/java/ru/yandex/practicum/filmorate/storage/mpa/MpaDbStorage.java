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
        return namedParameterJdbcTemplate.query(MpaQuery.SELECT_ALL.getSql(), mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        assertMpaExists(id);
        final String sqlMpaSelectById = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                WHERE m.id = :id
                """.formatted(par(pID));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(MpaQuery.SELECT_BY_ID.getSql(), params, mpaRowMapper));
    }

    @Override
    public void assertMpaExists(int id) {
        final String sqlMpaExists = """
                SELECT EXISTS(SELECT 1 FROM mpa_ratings WHERE id = :id)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("id", id);
        Boolean exists = namedParameterJdbcTemplate.queryForObject(sqlMpaExists, params, Boolean.class);
        if (exists == null || !exists) {
            throw new NotFoundException("MPA рейтинг с ID " + id + " не найден.");
        }
    }
}
