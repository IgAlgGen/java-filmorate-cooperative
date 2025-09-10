package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);
    User update(User user);
    Optional<User> findById(int id);
    List<User> findAll();
    boolean deleteById(int id);
    boolean existsById(int id);
}
