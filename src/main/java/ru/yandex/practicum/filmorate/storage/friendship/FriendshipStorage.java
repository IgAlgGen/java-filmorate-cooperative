package ru.yandex.practicum.filmorate.storage.friendship;

import ru.yandex.practicum.filmorate.model.User;
import java.util.List;

public interface FriendshipStorage {
    void addFriend(int userId, int friendId);

    void removeFriend(int userId, int friendId);

    List<User> findFriendsOf(int userId);                // подтверждённые друзья

    List<User> findCommonFriends(int userId, int other); // пересечение подтверждённых друзей
}