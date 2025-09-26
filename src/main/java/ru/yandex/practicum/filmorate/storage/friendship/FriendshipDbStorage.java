package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.util.List;

import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.User.SQL_USER_EXISTS;
import static ru.yandex.practicum.filmorate.storage.sql.SqlKeys.Friendship.*;

@Repository
@RequiredArgsConstructor
@Qualifier("friendshipDbStorage")
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<User> USER_MAPPER = new UserRowMapper();

    @Override
    @Transactional
    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья.");
        }
        assertUserExists(userId);
        assertUserExists(friendId);

        // 1) Создаём/обновляем заявку userId->friendId = UNCONFIRMED
        jdbcTemplate.update(SQL_FRIENDSHIP_UPSERT, userId, friendId, FriendshipStatus.UNCONFIRMED.name());

        // 2) Если есть встречная заявка friendId->userId, делаем ОБЕ записи CONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        if (reciprocal != null) {
            jdbcTemplate.update(
                    SQL_FRIENDSHIP_UPDATE_BOTH_CONFIRMED,
                    FriendshipStatus.CONFIRMED.name(),
                    userId, friendId,
                    friendId, userId
            );
        }
    }

    @Override
    @Transactional
    public void removeFriend(int userId, int friendId) {
        assertUserExists(userId);
        assertUserExists(friendId);

        // Удаляем userId -> friendId
        jdbcTemplate.update(SQL_FRIENDSHIP_DELETE, userId, friendId);

        // Если у друга было CONFIRMED — понижаем до UNCONFIRMED
        FriendshipStatus reciprocal = getReciprocalStatus(friendId, userId);
        if (reciprocal == FriendshipStatus.CONFIRMED) {
            jdbcTemplate.update(SQL_FRIENDSHIP_UPDATE_DEMOTE_TO_UNCONFIRMED,
                    FriendshipStatus.UNCONFIRMED.name(), friendId, userId);
        }
    }

    @Override
    public List<User> findFriendsOf(int userId) {
        assertUserExists(userId);
        return jdbcTemplate.query(SQL_FRIENDSHIP_GET_ALL, USER_MAPPER, userId);
    }

    @Override
    public List<User> findCommonFriends(int userId, int otherId) {
        assertUserExists(userId);
        assertUserExists(otherId);
        return jdbcTemplate.query(SQL_FRIENDSHIP_COMMON, USER_MAPPER, userId, otherId);
    }

    private void assertUserExists(int id) {
        Boolean ok = jdbcTemplate.queryForObject(SQL_USER_EXISTS, Boolean.class, id);
        if (!ok) {
            throw new NotFoundException("Пользователь не найден: id=" + id);
        }
    }

    private FriendshipStatus getReciprocalStatus(int from, int to) {
        try {
            String s = jdbcTemplate.queryForObject(SQL_FRIENDSHIP_RECIPROCAL, String.class, from, to);
            return s == null ? null : FriendshipStatus.valueOf(s);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}