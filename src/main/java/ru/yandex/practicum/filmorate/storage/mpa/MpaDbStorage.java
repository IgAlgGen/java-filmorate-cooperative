package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.Mpa.*;


@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper MAPPER = new MpaRowMapper();

    @Override
    public List<MpaRating> findAll() {
        return jdbcTemplate.query(SQL_MPA_SELECT_ALL, MAPPER);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        List<MpaRating> list = jdbcTemplate.query(SQL_MPA_SELECT_BY_ID, MAPPER, id);
        return list.stream().findFirst();
    }
}
