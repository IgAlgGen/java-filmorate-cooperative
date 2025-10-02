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

    private final String pID = "id";
    private final String pEMAIL = "email";
    private final String pLOGIN = "login";
    private final String pNAME = "name";
    private final String pBIRTHDAY = "birthday";

    private static String par(String param) {
        return ":" + param;
    }

    @Override
    @Transactional
    public User create(User user) {
        final String SQLUSERINSERT = """
                INSERT INTO users (email, login, name, birthday)
                VALUES (%s, %s, %s, %s)
                """.formatted(par(pEMAIL), par(pLOGIN), par(pNAME), par(pBIRTHDAY));
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(SQLUSERINSERT, params, keyHolder, new String[]{pID});
        Number key = keyHolder.getKey();
        user.setId(Objects.requireNonNull(key).intValue());
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        final String SQLUSERUPDATE = """
                UPDATE users SET email = %s, login = %s, name = %s, birthday = %s
                WHERE id = %s
                """.formatted(par(pEMAIL), par(pLOGIN), par(pNAME), par(pBIRTHDAY), par(pID));
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        int updated = namedParameterJdbcTemplate.update(SQLUSERUPDATE, params);
        if (updated == 0) {
            throw new NotFoundException("User not found: id=" + user.getId());
        }
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        final String SQLUSERSELECTBYID = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                WHERE u.id = %s
                """.formatted(par(pID));
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(SQLUSERSELECTBYID,
                    new MapSqlParameterSource(pID, id), userRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        final String SQLUSERSELECTALL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM users u
                ORDER BY u.id
                """;
        return namedParameterJdbcTemplate.query(SQLUSERSELECTALL, userRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        final String SQLUSERDELETE = """
                DELETE FROM users WHERE id = %s
                """.formatted(par(pID));
        return namedParameterJdbcTemplate.update(SQLUSERDELETE,
                new MapSqlParameterSource(pID, id)) > 0;
    }
}
