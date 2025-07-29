package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;
import ru.yandex.practicum.filmorate.service.InMemoryStorageImpl;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final InMemoryStorage<User> userStorage = new InMemoryStorageImpl<>();

    // Добавьте в классы-контроллеры эндпоинты с подходящим типом запроса для каждого из случаев:
    // 1. создание пользователя;
    // 2. обновление пользователя;
    // 3. получение списка всех пользователей.

    @PostMapping
    public ResponseEntity<User> addUser(User user) {
        User addedUser = userStorage.create(user);
        URI location = URI.create("/users/" + addedUser.getId());
        return ResponseEntity.created(location).body(addedUser); // Возвращаем ответ с кодом 201 Created и заголовком Location
    }

    @PutMapping
    public ResponseEntity<User> updateUser(int id, User user) {
        return userStorage.update(id, user)
                .map(updatedUser -> ResponseEntity.ok(updatedUser))
                .orElse(ResponseEntity.notFound().build()); // Возвращаем 404 Not Found, если пользователь не найден
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userStorage.findAll()); // Возвращаем список всех пользователей с кодом 200
    }
}
