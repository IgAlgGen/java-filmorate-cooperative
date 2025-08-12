package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static ru.yandex.practicum.filmorate.service.validators.UserValidator.validateUser;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final InMemoryStorage<User> userStorage;

    @PostMapping()
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
    public ResponseEntity<User> updateUser(@Valid @RequestBody(required = false) User user) {
        log.info("Обновление пользователя с ID {}: {}", user.getId(), user.toString());
        if (user == null) {
            log.error("Пустой JSON в запросе обновления пользователя");
            return ResponseEntity.status(500).build(); // возвращаем 500 Internal Server Error если пользователь не указан
        }
        try {
            validateUser(user);
            return userStorage.update(user.getId(), user)
                    .map(updatedUser -> {
                        log.info("Пользователь с ID {} обновлен: {}", user.getId(), updatedUser);
                        return ResponseEntity.ok(updatedUser);
                    })
                    .orElseGet(() -> {
                        log.warn("Пользователь с ID {} не найден", user.getId());
                        return ResponseEntity.status(404).body(user); // Возвращаем 404 Not Found, если пользователь не найден
                    });
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
}
