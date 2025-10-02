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

    final String pUSERID = "userId";
    final String pFRIENDID = "friendId";
    final String pSTATUS = "status";

    private static String par(String name) {
        return ":" + name;
    }

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        final String sqlFriendshipUpsert = """
                MERGE INTO friendships (requester_id, addressee_id, status)
                KEY (requester_id, addressee_id) VALUES (%s, %s, %s)
                """.formatted(par(pUSERID), par(pFRIENDID), par(pSTATUS));
        final String sqlFriendshipUpdateBothConfirmed = """
                UPDATE friendships SET status = %s
                WHERE (requester_id = %s AND addressee_id = %s) OR (requester_id = %s AND addressee_id = %s)
                """.formatted(par(pSTATUS), par(pUSERID), par(pFRIENDID), par(pFRIENDID), par(pUSERID));
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);
        // 1) Создаём/обновляем заявку userId->friendId = UNCONFIRMED
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pUSERID, userId)
                .addValue(pFRIENDID, friendId)
                .addValue(pSTATUS, FriendshipStatus.UNCONFIRMED.name());
        namedParameterJdbcTemplate.update(sqlFriendshipUpsert, params);
        // 2) Если есть встречная заявка friendId->userId, делаем ОБЕ записи CONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        if (reciprocal != null) {
            MapSqlParameterSource updateParams = new MapSqlParameterSource()
                    .addValue(pSTATUS, FriendshipStatus.CONFIRMED.name())
                    .addValue(pUSERID, userId)
                    .addValue(pFRIENDID, friendId);
            namedParameterJdbcTemplate.update(sqlFriendshipUpdateBothConfirmed, updateParams);
        }
    }

    @Override
    @Transactional
    public void removeFriend(int userId, int friendId) {
        final String sqlFriendshipDelete = """
                DELETE FROM friendships
                WHERE requester_id = %s AND addressee_id = %s
                """.formatted(par(pUSERID), par(pFRIENDID));
        final String sqlFriendshipUpdateDemoteToUnconfirmed = """
                UPDATE friendships SET status = %s
                WHERE requester_id = %s AND addressee_id = %s
                """.formatted(par(pSTATUS), par(pUSERID), par(pFRIENDID));
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя удалить себя из друзей.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pUSERID, userId)
                .addValue(pFRIENDID, friendId);
        // Удаляем userId -> friendId
        namedParameterJdbcTemplate.update(sqlFriendshipDelete, params);
        // Если у друга было CONFIRMED — понижаем до UNCONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        params.addValue(pSTATUS, FriendshipStatus.UNCONFIRMED.name());
        if (reciprocal == FriendshipStatus.CONFIRMED) {
            namedParameterJdbcTemplate.update(sqlFriendshipUpdateDemoteToUnconfirmed, params);
        }
    }

    @Override
    public List<User> findFriendsOf(int userId) {
        final String sqlFriendshipGetAll = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f
                JOIN users u ON u.id = f.addressee_id
                WHERE f.requester_id = %s AND f.status = 'CONFIRMED'
                ORDER BY u.id
                """.formatted(par(pUSERID));
        assertUserExists(userId);
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pUSERID, userId);
        return namedParameterJdbcTemplate.query(sqlFriendshipGetAll, params, userRowMapper);
    }

    @Override
    public List<User> findCommonFriends(int userId, int friendId) {
        final String sqlFriendshipCommon = """
                SELECT u.id, u.email, u.login, u.name, u.birthday
                FROM friendships f1
                JOIN friendships f2 ON f1.addressee_id = f2.addressee_id
                    AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
                JOIN users u ON u.id = f1.addressee_id
                WHERE f1.requester_id = %s AND f2.requester_id = %s
                ORDER BY u.id
                """.formatted(par(pUSERID), par(pFRIENDID));
        assertUserExists(userId);
        assertUserExists(friendId);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue(pUSERID, userId)
                .addValue(pFRIENDID, friendId);
        return namedParameterJdbcTemplate.query(sqlFriendshipCommon, params, userRowMapper);
    }

    private void assertUserExists(int userId) {
        final String sqlUserExists = """
                SELECT EXISTS(SELECT 1 FROM users WHERE id = %s)
                """.formatted(par(pUSERID));
        MapSqlParameterSource params = new MapSqlParameterSource().addValue(pUSERID, userId);
        Boolean ok = namedParameterJdbcTemplate.queryForObject(sqlUserExists, params, Boolean.class);
        if (Boolean.FALSE.equals(ok)) {
            throw new NotFoundException("Пользователь не найден: id=" + userId);
        }
    }

    private FriendshipStatus getReciprocalStatus(int friendId, int userId) {
        final String sqlFriendshipReciprocal = """
                SELECT status FROM friendships
                WHERE requester_id = %s AND addressee_id = %s
                """.formatted(par(pUSERID), par(pFRIENDID));
        try {
            String s = namedParameterJdbcTemplate.queryForObject(sqlFriendshipReciprocal, Map.of(pUSERID, friendId, pFRIENDID, userId), String.class);
            return s == null ? null : FriendshipStatus.valueOf(s);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}