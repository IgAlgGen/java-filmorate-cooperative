package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Qualifier("friendshipDbStorage")
public class FriendshipDbStorage implements FriendshipStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);
        // 1) Создаём/обновляем заявку userId->friendId = UNCONFIRMED
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId)
                .addValue("status", FriendshipStatus.UNCONFIRMED.name());
        namedParameterJdbcTemplate.update(FriendshipQuery.INSERT.getSql(), params);
        // 2) Если есть встречная заявка friendId->userId, делаем ОБЕ записи CONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        if (reciprocal != null) {
            MapSqlParameterSource updateParams = new MapSqlParameterSource()
                    .addValue("status", FriendshipStatus.CONFIRMED.name())
                    .addValue("userId", userId)
                    .addValue("friendId", friendId);
            namedParameterJdbcTemplate.update(FriendshipQuery.UPDATE_BOTH_CONFIRMED.getSql(), updateParams);
        }
    }

    @Override
    @Transactional
    public void removeFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя удалить себя из друзей.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        // Удаляем userId -> friendId
        namedParameterJdbcTemplate.update(FriendshipQuery.DELETE.getSql(), params);

        // Если у друга было CONFIRMED — понижаем до UNCONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        params.addValue("status", FriendshipStatus.UNCONFIRMED.name());
        if (reciprocal == FriendshipStatus.CONFIRMED) {
            namedParameterJdbcTemplate.update(FriendshipQuery.UPDATE.getSql(), params);
        }
    }

    @Override
    public List<User> findFriendsOf(int userId) {
        assertUserExists(userId);
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);
        return namedParameterJdbcTemplate.query(FriendshipQuery.SELECT_FRIENDS_BY_USER_ID.getSql(), params, userRowMapper);
    }

    @Override
    public List<User> findCommonFriends(int userId, int friendId) {
        assertUserExists(friendId);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        return namedParameterJdbcTemplate.query(FriendshipQuery.SELECT_COMMON_FRIENDS.getSql(), params, userRowMapper);
    }

    private void assertUserExists(int userId) {
        final String sqlUserExists = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = :userId)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("userId", userId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(sqlUserExists, params, Boolean.class);
        if (Boolean.FALSE.equals(ok)) {
            throw new NotFoundException("Пользователь не найден: id=" + userId);
        }
    }

    private FriendshipStatus getReciprocalStatus(int friendId, int userId) {
        final String sql = """
                SELECT status FROM friendships
                WHERE requester_id = :userId AND addressee_id = :friendId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);
        try {
            String s = namedParameterJdbcTemplate.queryForObject(sql, params, String.class);
            return s == null ? null : FriendshipStatus.valueOf(s);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}