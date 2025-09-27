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

    final String P_USER_ID = "userId";
    final String P_FRIEND_ID = "friendId";
    final String P_STATUS = "status";

    private static String par(String name) {
        return ":" + name;
    }

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        final String SQL_FRIENDSHIP_UPSERT = """
                MERGE INTO friendships (requester_id, addressee_id, status)
                KEY (requester_id, addressee_id) VALUES (%s, %s, %s)
                """.formatted(par(P_USER_ID), par(P_FRIEND_ID), par(P_STATUS));
        final String SQL_FRIENDSHIP_UPDATE_BOTH_CONFIRMED = """
                UPDATE friendships SET status = %s
                WHERE (requester_id = %s AND addressee_id = %s) OR (requester_id = %s AND addressee_id = %s)
                """.formatted(par(P_STATUS), par(P_USER_ID), par(P_FRIEND_ID), par(P_FRIEND_ID), par(P_USER_ID));
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);
        // 1) Создаём/обновляем заявку userId->friendId = UNCONFIRMED
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(P_USER_ID, userId)
                .addValue(P_FRIEND_ID, friendId)
                .addValue(P_STATUS, FriendshipStatus.UNCONFIRMED.name());
        namedParameterJdbcTemplate.update(SQL_FRIENDSHIP_UPSERT, params);
        // 2) Если есть встречная заявка friendId->userId, делаем ОБЕ записи CONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        if (reciprocal != null) {
            MapSqlParameterSource updateParams = new MapSqlParameterSource()
                    .addValue(P_STATUS, FriendshipStatus.CONFIRMED.name())
                    .addValue(P_USER_ID, userId)
                    .addValue(P_FRIEND_ID, friendId);
            namedParameterJdbcTemplate.update(SQL_FRIENDSHIP_UPDATE_BOTH_CONFIRMED, updateParams);
        }
    }

    @Override
    @Transactional
    public void removeFriend(int userId, int friendId) {
        final String SQL_FRIENDSHIP_DELETE = """
                DELETE FROM friendships
                WHERE requester_id = %s AND addressee_id = %s
                """.formatted(par(P_USER_ID), par(P_FRIEND_ID));
        final String SQL_FRIENDSHIP_UPDATE_DEMOTE_TO_UNCONFIRMED = """
                UPDATE friendships SET status = %s
                WHERE requester_id = %s AND addressee_id = %s
                """.formatted(par(P_STATUS), par(P_USER_ID), par(P_FRIEND_ID));
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя удалить себя из друзей.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(P_USER_ID, userId)
                .addValue(P_FRIEND_ID, friendId);
        // Удаляем userId -> friendId
        namedParameterJdbcTemplate.update(SQL_FRIENDSHIP_DELETE, params);
        // Если у друга было CONFIRMED — понижаем до UNCONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        params.addValue(P_STATUS, FriendshipStatus.UNCONFIRMED.name());
        if (reciprocal == FriendshipStatus.CONFIRMED) {
            namedParameterJdbcTemplate.update(SQL_FRIENDSHIP_UPDATE_DEMOTE_TO_UNCONFIRMED, params);
        }
    }

    @Override
    public List<User> findFriendsOf(int userId) {
        final String SQL_FRIENDSHIP_GET_ALL = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.id = f.addressee_id
                WHERE f.requester_id = %s AND f.status = 'CONFIRMED'
                ORDER BY u.id
                """.formatted(par(P_USER_ID));
        assertUserExists(userId);
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_USER_ID, userId);
        return namedParameterJdbcTemplate.query(SQL_FRIENDSHIP_GET_ALL, params, userRowMapper);
    }

    @Override
    public List<User> findCommonFriends(int userId, int friendId) {
        final String SQL_FRIENDSHIP_COMMON = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f1
                JOIN friendships f2 ON f1.addressee_id = f2.addressee_id
                    AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
                JOIN users u ON u.id = f1.addressee_id
                WHERE f1.requester_id = %s AND f2.requester_id = %s
                ORDER BY u.id
                """.formatted(par(P_USER_ID), par(P_FRIEND_ID));
        assertUserExists(userId);
        assertUserExists(friendId);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(P_USER_ID, userId)
                .addValue(P_FRIEND_ID, friendId);
        return namedParameterJdbcTemplate.query(SQL_FRIENDSHIP_COMMON, params, userRowMapper);
    }

    private void assertUserExists(int userId) {
        final String SQL_USER_EXISTS = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = %s)
                """.formatted(par(P_USER_ID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(P_USER_ID, userId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(SQL_USER_EXISTS, params, Boolean.class);
        if (Boolean.FALSE.equals(ok)) {
            throw new NotFoundException("Пользователь не найден: id=" + userId);
        }
    }

    private FriendshipStatus getReciprocalStatus(int friendId, int userId) {
        final String SQL_FRIENDSHIP_RECIPROCAL = """
                SELECT status FROM friendships
                WHERE requester_id = %s AND addressee_id = %s
                """.formatted(par(P_USER_ID), par(P_FRIEND_ID));
        try {
            String s = namedParameterJdbcTemplate.queryForObject(SQL_FRIENDSHIP_RECIPROCAL, Map.of(P_USER_ID, friendId, P_FRIEND_ID, userId), String.class);
            return s == null ? null : FriendshipStatus.valueOf(s);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}