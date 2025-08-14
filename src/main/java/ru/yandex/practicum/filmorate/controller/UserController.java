package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.service.validators.UserValidator;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserValidator userValidator;

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        try {
            userValidator.validateUser(user);
            User created = userService.create(user);
            log.info("Пользователь добавлен: {}", created.toString());
            URI location = URI.create("/users/" + created.getId());
            return ResponseEntity.created(location).body(created); // Возвращаем ответ с кодом 201 Created и заголовком Location
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
            userValidator.validateUser(user);
            User updated = userService.update(user.getId(), user);
            log.info("Пользователь с ID {} обновлен: {}", user.getId(), updated);
            return ResponseEntity.ok(updated);
//            return userService.update(user.getId(), user)
//                    .map(updatedUser -> {
//                        log.info("Пользователь с ID {} обновлен: {}", user.getId(), updatedUser);
//                        return ResponseEntity.ok(updatedUser);
//                    })
//                    .orElseGet(() -> {
//                        log.warn("Пользователь с ID {} не найден", user.getId());
//                        return ResponseEntity.status(404).body(user); // Возвращаем 404 Not Found, если пользователь не найден
//                    });
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при обновлении пользователя: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        log.info("Получение списка всех пользователей");
        return ResponseEntity.ok(userService.getAll()); // Возвращаем список всех пользователей с кодом 200
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable int id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    // ДРУЖБА

    /*
    добавление в друзья.
     */
    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    /*
    удаление из друзей.
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.removeFriend(id, friendId);
        return ResponseEntity.ok().build();
    }

    /*
    возвращаем список пользователей, являющихся его друзьями.
     */
    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable int id) {
        return ResponseEntity.ok(userService.getFriends(id));
    }

    /*
    список друзей, общих с другим пользователем.
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return ResponseEntity.ok(userService.getCommonFriends(id, otherId));
    }
}
