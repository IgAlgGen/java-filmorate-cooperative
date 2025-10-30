package ru.yandex.practicum.filmorate.storage.filmLike;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("filmLikeDbStorage")
public class FilmLikeDbStorage implements FilmLikeStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    @Transactional
    public void addLike(int filmId, int userId) {
        assertFilmExists(filmId);
        assertUserExists(userId);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        namedParameterJdbcTemplate.update(FilmLikeQuery.INSERT.getSql(), mapSqlParameterSource);
    }

    @Override
    @Transactional
    public boolean removeLike(int filmId, int userId) {
        assertFilmExists(filmId);
        assertUserExists(userId);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("userId", userId);
        return namedParameterJdbcTemplate.update(FilmLikeQuery.DELETE_BY_USER_ID_AND_FILM_ID.getSql(),
                mapSqlParameterSource) > 0;
    }

    @Override
    public List<Film> findPopular(int limit, Long genreId, Integer year) {
        if (limit <= 0) return Collections.emptyList();
        if (genreId != null) assertGenreExists(genreId);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("genreId", genreId)
                .addValue("year", year)
                .addValue("limit", limit);

        return namedParameterJdbcTemplate.query(FilmLikeQuery.SELECT_POPULAR.getSql(), params, filmRowMapper);
    }


    /**
     * Функция извлекает все записи из таблицы film_likes, сгруппированные по id пользователя.
     * Извлеченные данные возвращаются в виде Map, где:
     * ключ - id пользователя,
     * значение - множество (Set) id фильмов, которым пользователь поставил лайк.
     */
    @Override
    public Map<Integer, Set<Integer>> getUsersLikesData() {
        Map<Integer, Set<Integer>> usersLikesData = new HashMap<>();
        namedParameterJdbcTemplate.query(
                FilmLikeQuery.SELECT_LIKES.getSql(),
                rs -> {
                    int userId = rs.getInt("user_id");
                    int filmId = rs.getInt("film_id");
                    if (!usersLikesData.containsKey(userId)) {
                        usersLikesData.put(userId, new HashSet<>());
                    }
                    Set<Integer> userLikedFilms = usersLikesData.get(userId);
                    userLikedFilms.add(filmId);
                }
        );
        return usersLikesData;
    }

    private void assertFilmExists(int filmId) {
        final String sqlFilmExists = """
                SELECT EXISTS(SELECT 1 FROM films WHERE id = :filmId)
                """;
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("filmId", filmId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(sqlFilmExists, mapSqlParameterSource, Boolean.class);
        if (Boolean.FALSE.equals(ok)) throw new NotFoundException("Фильм не найден: id=" + filmId);
    }

    private void assertUserExists(int userId) {
        final String sqlUserExists = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId)
                """;
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource()
                .addValue("userId", userId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(sqlUserExists,
                mapSqlParameterSource, Boolean.class);
        if (Boolean.FALSE.equals(ok)) throw new NotFoundException("Пользователь не найден: id=" + userId);
    }

    private void assertGenreExists(long genreId) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM genres WHERE id = :gid)";
        Boolean ok = namedParameterJdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource("gid", genreId),
                Boolean.class
        );
        if (Boolean.FALSE.equals(ok)) {
            throw new NotFoundException("Жанр не найден: id=" + genreId);
        }
    }
}