package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Repository
@RequiredArgsConstructor
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserRowMapper userRowMapper;

    private final String P_ID = "id";
    private final String P_EMAIL = "email";
    private final String P_LOGIN = "login";
    private final String P_NAME = "name";
    private final String P_BIRTHDAY = "birthday";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    @Transactional
    public User create(User user) {
        final String SQL_USER_INSERT = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (%s, %s, %s, %s)
                """.formatted(par(P_EMAIL), par(P_LOGIN), par(P_NAME), par(P_BIRTHDAY));
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(SQL_USER_INSERT, params, keyHolder, new String[]{P_ID});
        Number key = keyHolder.getKey();
        user.setId(Objects.requireNonNull(key).intValue());
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        final String SQL_USER_UPDATE = """
                UPDATE users SET email = %s, login = %s, name = %s, birthday = %s
                WHERE id = %s
                """.formatted(par(P_EMAIL), par(P_LOGIN), par(P_NAME), par(P_BIRTHDAY), par(P_ID));
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        int updated = namedParameterJdbcTemplate.update(SQL_USER_UPDATE, params);
        if (updated == 0) {
            throw new NotFoundException("User not found: id=" + user.getId());
        }
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        final String SQL_USER_SELECT_BY_ID = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                WHERE u.id = %s
                """.formatted(par(P_ID));
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(SQL_USER_SELECT_BY_ID,
                    new MapSqlParameterSource(P_ID, id), userRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        final String SQL_USER_SELECT_ALL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                ORDER BY u.id
                """;
        return namedParameterJdbcTemplate.query(SQL_USER_SELECT_ALL, userRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        final String SQL_USER_DELETE = """
                DELETE FROM users WHERE id = %s
                """.formatted(par(P_ID));
        return namedParameterJdbcTemplate.update(SQL_USER_DELETE,
                new MapSqlParameterSource(P_ID, id)) > 0;
    }
}
