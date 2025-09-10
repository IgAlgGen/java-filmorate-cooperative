package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;

    @Qualifier("friendshipDbStorage")
    private final FriendshipStorage friendshipStorage;


    public User create(User u) {
        return userStorage.create(u);
    }

    public User update(User u) {
        return userStorage.update(u);
    }

    public User get(int id) {
        return userStorage.findById(id).orElseThrow();
    }

    public List<User> getAll() {
        return userStorage.findAll();
    }

    public void delete(int id) {
        userStorage.deleteById(id);
    }

    public boolean exists(int id) {
        return userStorage.existsById(id);
    }

    public void addFriend(int userId, int friendId) {
        // (опционально) checkUserExistence(userId); checkUserExistence(friendId);
        friendshipStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        friendshipStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        return friendshipStorage.findFriendsOf(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return friendshipStorage.findCommonFriends(userId, otherId);
    }

}
