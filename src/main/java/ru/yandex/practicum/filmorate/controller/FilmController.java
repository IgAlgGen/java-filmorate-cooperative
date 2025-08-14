package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.validators.FilmValidator;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final FilmValidator filmValidator;

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film.toString());
        try {
            filmValidator.validateFilm(film);
            Film created = filmService.create(film);
            log.info("Фильм добавлен: {}", created.toString());
            URI location = URI.create("/films/" + created.getId());
            return ResponseEntity.created(location).body(created);
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
            filmValidator.validateFilm(film);
            Film updated = filmService.update(film.getId(), film);
            return ResponseEntity.ok(updated);
//            return filmService.update(film.getId(), film)
//                    .map(updatedFilm -> {
//                        log.info("Фильм с ID {} обновлен: {}", film.getId(), updatedFilm);
//                        return ResponseEntity.ok(updatedFilm);
//                    })
//                    .orElseGet(() -> {
//                        log.warn("Фильм с ID {} не найден", film.getId());
//                        return ResponseEntity.status(404).body(film); // Возвращаем 404 Not Found, если фильм не найден
//                    });
        } catch (ValidationException e) {
            log.warn("Ошибка при обновлении фильма: {}", e.getMessage());
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAll() {
        log.info("Получение списка всех фильмов");
        return ResponseEntity.ok(filmService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable int id) {
        return ResponseEntity.ok(filmService.getById(id));
    }

    // ЛАЙКИ И ПОПУЛЯРНЫЕ

    /*
    пользователь ставит лайк фильму
     */
    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable int id, @PathVariable int userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    /*
    пользователь удаляет лайк.
     */
    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable int id, @PathVariable int userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    /*
    Возвращает список из первых count фильмов по количеству лайков.
    Если значение параметра count не задано, верните первые 10.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopular(@RequestParam(defaultValue = "10") int count) {
        return ResponseEntity.ok(filmService.getPopular(count));
    }
}
