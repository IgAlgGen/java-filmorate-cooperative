package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static ru.yandex.practicum.filmorate.service.validators.FilmValidator.validateFilm;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final InMemoryStorage<Film> filmStorage;

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
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody(required = false) Film film) {
        log.info("Обновление фильма с ID {}: {}", film.getId(), film.toString());
        if (film == null) {
            log.error("Пустой JSON в запросе обновления фильма");
            return ResponseEntity.status(500).build(); // возвращаем 500 Internal Server Error (как хотят тесты в postman)
        }
        try {
            validateFilm(film);
            return filmStorage.update(film.getId(), film)
                    .map(updatedFilm -> {
                        log.info("Фильм с ID {} обновлен: {}", film.getId(), updatedFilm);
                        return ResponseEntity.ok(updatedFilm);
                    })
                    .orElseGet(() -> {
                        log.warn("Фильм с ID {} не найден", film.getId());
                        return ResponseEntity.status(404).body(film); // Возвращаем 404 Not Found, если фильм не найден
                    });
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
}
