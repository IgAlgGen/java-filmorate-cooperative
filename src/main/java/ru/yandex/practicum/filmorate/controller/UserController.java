package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;
import ru.yandex.practicum.filmorate.service.InMemoryStorageImpl;

import java.net.URI;
import java.time.LocalDate;
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
        validateUser(user);
        User addedUser = userStorage.create(user);
        URI location = URI.create("/users/" + addedUser.getId());
        return ResponseEntity.created(location).body(addedUser); // Возвращаем ответ с кодом 201 Created и заголовком Location
    }

    @PutMapping
    public ResponseEntity<User> updateUser(int id, User user) {
        validateUser(user);
        return userStorage.update(id, user)
                .map(updatedUser -> ResponseEntity.ok(updatedUser))
                .orElse(ResponseEntity.notFound().build()); // Возвращаем 404 Not Found, если пользователь не найден
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userStorage.findAll()); // Возвращаем список всех пользователей с кодом 200
    }

    // валидация полей User
    private void validateUser(User user) {
        // email
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email не может быть пустым и должен содержать символ '@'.");
        }
        // login
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы.");
        }
        // имя: если пустое, подставляем login
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        // день рождения
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }
}
