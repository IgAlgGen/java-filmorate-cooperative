package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film f = new Film();
        f.setId(rs.getInt("film_id"));
        f.setName(rs.getString("name"));
        f.setDescription(rs.getString("description"));
        LocalDate releaseDate = rs.getDate("release_date") != null ? rs.getDate("release_date").toLocalDate() : null;
        f.setReleaseDate(releaseDate);
        f.setDuration(rs.getInt("duration"));
        String mpaCode = rs.getString("mpa");
        if (mpaCode != null) {
            f.setMpa(MpaRating.fromId(Integer.parseInt(mpaCode)));
        }
        return f;
    }
}
