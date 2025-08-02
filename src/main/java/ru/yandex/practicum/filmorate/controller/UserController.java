package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;
import ru.yandex.practicum.filmorate.service.InMemoryStorageImpl;

import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final InMemoryStorage<User> userStorage = new InMemoryStorageImpl<>();

    // Добавьте в классы-контроллеры эндпоинты с подходящим типом запроса для каждого из случаев:
    // 1. создание пользователя;
    // 2. обновление пользователя;
    // 3. получение списка всех пользователей.

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody @Valid User user) {
        log.info("Добавление пользователя: {}", user.toString());
        try {
            validateUser(user);
            User addedUser = userStorage.create(user);
            log.info("Пользователь добавлен: {}", addedUser.toString());
            URI location = URI.create("/users/" + addedUser.getId());
            return ResponseEntity.created(location).body(addedUser); // Возвращаем ответ с кодом 201 Created и заголовком Location
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при добавлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public ResponseEntity<?> updateUser(@Valid@ RequestBody(required = false) User user) {
        log.info("Обновление пользователя с ID {}: {}", user.getId(), user.toString());
        try {
            if (user == null) {
                log.error("Пустой JSON в запросе обновления пользователя");
                Map<String, String> body = Collections.singletonMap(
                        "error",
                        "Пустой JSON в запросе обновления пользователя"
                );
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body); // возвращаем 500 Internal Server Error (как хотят тесты в postman)
            } else {
                validateUser(user);
                return userStorage.update(user.getId(), user)
                        .map(updatedUser -> {
                            log.info("Пользователь с ID {} обновлен: {}", user.getId(), updatedUser);
                            return ResponseEntity.ok(updatedUser);
                        })
                        .orElseGet(() -> {
                            log.warn("Пользователь с ID {} не найден", user.getId());
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                        });
            }
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return ResponseEntity.ok(userStorage.findAll()); // Возвращаем список всех пользователей с кодом 200
    }

    // валидация полей User
    void validateUser(User user) {
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
