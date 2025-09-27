package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    private final String PARAM_ID = "id";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    public List<MpaRating> findAll() {
        final String SQL_MPA_SELECT_ALL = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                ORDER BY m.id
                """;
        return namedParameterJdbcTemplate.query(SQL_MPA_SELECT_ALL, mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        final String SQL_MPA_SELECT_BY_ID = """
                SELECT m.id, m.name
                FROM mpa_ratings m
                WHERE m.id = %s
                """.formatted(par(PARAM_ID));
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(PARAM_ID, id);
        return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(SQL_MPA_SELECT_BY_ID, params, mpaRowMapper));
    }
}
