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

    @Override
    @Transactional
    public User create(User user) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(UserQuery.INSERT.getSql(), params, keyHolder, new String[]{"id"});
        Number key = keyHolder.getKey();
        user.setId(Objects.requireNonNull(key).intValue());
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        int updated = namedParameterJdbcTemplate.update(UserQuery.UPDATE.getSql(), params);
        if (updated == 0) {
            throw new NotFoundException("User not found: id=" + user.getId());
        }
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        try {
            return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(UserQuery.SELECT_BY_ID.getSql(),
                    new MapSqlParameterSource("id", id), userRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        return namedParameterJdbcTemplate.query(UserQuery.SELECT_ALL.getSql(), userRowMapper);
    }

    @Override
    @Transactional
    public boolean deleteById(int id) {
        return namedParameterJdbcTemplate.update(UserQuery.DELETE_BY_ID.getSql(),
                new MapSqlParameterSource("id", id)) > 0;
    }
}
