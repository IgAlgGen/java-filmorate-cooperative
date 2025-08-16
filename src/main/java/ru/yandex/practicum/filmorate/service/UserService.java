package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.InMemoryStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final InMemoryStorage<User> userStorage;

    public User getById(int id) {
        return userStorage.findById(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id=%d не найден", id)));
    }

    public List<User> getAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(int id, User user) {
        return userStorage.update(id, user).orElseThrow(() ->
                new NotFoundException(String.format("Пользователь с id=%d не найден", id)));
    }

    // Друзья

    public void addFriend(int userId, int friendId) {
        if (userId == friendId) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья");
        }
        User u1 = getById(userId);
        User u2 = getById(friendId);
        u1.addFriend(friendId);
        u2.addFriend(userId);
    }

    public void removeFriend(int userId, int friendId) {
        User u1 = getById(userId);
        User u2 = getById(friendId);
        u1.removeFriend(friendId);
        u2.removeFriend(userId);
    }

    public List<User> getFriends(int userId) {
        User user = getById(userId);
        return user.getFriends().stream()
                .map(this::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User u1 = getById(userId);
        User u2 = getById(otherId);
        Set<Integer> common = u1.getFriends().stream()
                .filter(u2.getFriends()::contains)
                .collect(Collectors.toSet());
        return common.stream().map(this::getById).collect(Collectors.toList());
    }
}
