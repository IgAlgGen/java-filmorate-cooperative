package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;


@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        log.info("Добавление пользователя: {}", user.toString());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        User created = userService.create(user);
        log.info("Пользователь добавлен: {}", created.toString());
        URI location = URI.create("/users/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping
    public ResponseEntity<User> update(@Valid @RequestBody User user) {
        log.info("Обновление пользователя с ID {}: {}", user.getId(), user.toString());
        User updated = userService.update(user);
        log.info("Пользователь с ID {} обновлен: {}", user.getId(), updated);
        return ResponseEntity.ok(updated);
    }


    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        log.info("Получение списка всех пользователей");
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable @Positive int id) {
        log.info("Получение пользователя с ID {}", id);
        return ResponseEntity.ok(userService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive int id) {
        log.info("Удаление пользователя с ID {}", id);
        userService.delete(id);
        log.info("Пользователь с ID {} удален", id);
        return ResponseEntity.noContent().build();
    }

    /**
    Добавление в друзья.
     */
    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> addFriend(@PathVariable @Positive int id, @PathVariable @Positive int friendId) {
        log.info("Пользователь с ID {} добавляет в друзья пользователя с ID {}", id, friendId);
        userService.addFriend(id, friendId);
        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", id, friendId);
        return ResponseEntity.ok().build();
    }

    /**
    Удаление из друзей.
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Void> removeFriend(@PathVariable @Positive int id, @PathVariable @Positive int friendId) {
        log.info("Пользователь с ID {} удаляет из друзей пользователя с ID {}", id, friendId);
        userService.removeFriend(id, friendId);
        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", id, friendId);
        return ResponseEntity.ok().build();
    }

    /**
    Возвращаем список пользователей, являющихся его друзьями.
     */
    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable @Positive int id) {
        log.info("Получение списка друзей пользователя с ID {}", id);
        return ResponseEntity.ok(userService.getFriends(id));
    }

    /**
    Список друзей, общих с другим пользователем.
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getCommonFriends(@PathVariable @Positive int id, @PathVariable @Positive int otherId) {
        log.info("Получение списка общих друзей между пользователями с ID {} и ID {}", id, otherId);
        return ResponseEntity.ok(userService.getCommonFriends(id, otherId));
    }
}
