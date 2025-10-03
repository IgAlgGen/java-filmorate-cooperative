package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    @Qualifier("friendshipDbStorage")
    private final FriendshipStorage friendshipStorage;


    public User create(User u) {
        log.debug("Создание пользователя: email='{}', login='{}', birthday={}", u.getEmail(), u.getLogin(), u.getBirthday());
        User createdUser = userStorage.create(u);
        log.debug("Пользователь создан: id={}, login='{}'", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    public User update(User u) {
        log.debug("Обновление пользователя: id={}, email='{}', login='{}', birthday={}",
                u.getId(),
                u.getEmail(),
                u.getLogin(),
                u.getBirthday());
        User existingUser = userStorage.update(u);
        log.debug("Пользователь обновлен: id={}, login='{}'", existingUser.getId(), existingUser.getLogin());
        return existingUser;
    }

    public User getById(int id) {
        log.debug("Поиск пользователя по ID {}", id);
        User user = userStorage.getById(id).orElseThrow();
        log.debug("Найден пользователь: id={}, login='{}'", user.getId(), user.getLogin());
        return user;
    }

    public List<User> getAll() {
        log.debug("Поиск всех пользователей");
        List<User> list = userStorage.getAll();
        log.debug("Найдено {} пользователей", list.size());
        return list;
    }

    public void delete(int id) {
        log.debug("Удаление пользователя с ID {}", id);
        userStorage.deleteById(id);
        log.debug("Пользователь с ID {} удален", id);
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Пользователь с ID {} добавляет в друзья пользователя с ID {}", userId, friendId);
        friendshipStorage.addFriend(userId, friendId);
        log.debug("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        log.debug("Пользователь с ID {} удаляет из друзей пользователя с ID {}", userId, friendId);
        // (опционально) checkUserExistence(userId); checkUserExistence(friendId)
        friendshipStorage.removeFriend(userId, friendId);
        log.debug("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        log.debug("Получение списка друзей пользователя с ID {}", userId);
        List<User> friends = friendshipStorage.findFriendsOf(userId);
        log.debug("Найдено {} друзей пользователя с ID {}", friends.size(), userId);
        return friends;
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        log.debug("Получение списка общих друзей пользователей с ID {} и ID {}", userId, otherId);
        List<User> commonFriends = friendshipStorage.findCommonFriends(userId, otherId);
        log.debug("Найдено {} общих друзей пользователей с ID {} и ID {}", commonFriends.size(), userId, otherId);
        return commonFriends;
    }

}
