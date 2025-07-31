package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;
import ru.yandex.practicum.filmorate.service.InMemoryStorageImpl;


import javax.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final InMemoryStorage<Film> filmStorage = new InMemoryStorageImpl<>();

    // Добавьте в классы-контроллеры эндпоинты с подходящим типом запроса для каждого из случаев:
    // 1. добавление фильма;
    // 2. обновление фильма;
    // 3. получение всех фильмов.

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film.toString());
        try {
            validateFilm(film);
            Film addedFilm = filmStorage.create(film);
            log.info("Фильм добавлен: {}", addedFilm.toString());
            URI location = URI.create("/films/" + addedFilm.getId());
            return ResponseEntity.created(location).body(addedFilm);
        } catch (ValidationException e) {
            log.warn("Ошибка валидации при добавлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма с ID {}: {}", film.getId(), film.toString());
        try {
            if (film == null || film.getId() == 0) {
                log.error("Пустой JSON в запросе обновления фильма");
                return ResponseEntity.status(500).build(); // возвращаем 500 Internal Server Error (как хотят тесты в postman)
            } else {
                validateFilm(film);
                return filmStorage.update(film.getId(), film)
                        .map(updatedFilm -> {
                            log.info("Фильм с ID {} обновлен: {}", film.getId(), updatedFilm);
                            return ResponseEntity.ok(updatedFilm);
                        })
                        .orElseGet(() -> {
                            log.warn("Фильм с ID {} не найден", film.getId());
                            return ResponseEntity.notFound().build(); // Возвращаем 404 Not Found, если фильм не найден
                        });
            }
        } catch (ValidationException e) {
            log.warn("Ошибка при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        log.info("Получение списка всех фильмов");
        return ResponseEntity.ok(filmStorage.findAll());
    }

    // валидация полей Film
    void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Длина описания не должна превышать 200 символов.");
        }
        LocalDate earliest = LocalDate.of(1895, 12, 28);
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(earliest)) {
            throw new ValidationException(
                    String.format("Дата релиза не может быть раньше %s.", earliest)
            );
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

}
