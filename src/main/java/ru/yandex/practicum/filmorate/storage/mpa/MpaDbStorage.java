package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mapper = new MpaRowMapper();

    @Override
    public List<MpaRating> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM mpa_ratings ORDER BY id", mapper);
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        List<MpaRating> list = jdbcTemplate.query("SELECT id, name FROM mpa_ratings WHERE id = ?", mapper, id);
        return list.stream().findFirst();
    }
}
