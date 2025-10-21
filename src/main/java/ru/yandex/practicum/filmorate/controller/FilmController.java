package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<Film> create(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film.toString());
        Film created = filmService.create(film);
        URI location = URI.create("/films/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping
    public ResponseEntity<Film> update(@Valid @RequestBody(required = false) Film film) {
        log.info("Обновление фильма с ID {}: {}", film.getId(), film.toString());
        Film updated = filmService.update(film);
        log.info("Фильм с ID {} обновлен: {}", film.getId(), updated);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAll() {
        log.info("Получение списка всех фильмов");
        List<Film> films = filmService.getAll();
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable int id) {
        log.info("Получение фильма с ID {}", id);
        return ResponseEntity.ok(filmService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable @Positive int id) {
        log.info("Удаление фильма с ID {}", id);
        filmService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
    Пользователь ставит лайк фильму
     */
    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        log.info("Пользователь с ID {} ставит лайк фильму с ID {}", userId, id);
        filmService.addLike(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
    Пользователь удаляет лайк.
     */
    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable @Positive int id, @PathVariable @Positive int userId) {
        log.info("Пользователь с ID {} удаляет лайк к фильму с ID {}", userId, id);
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    /**
    Возвращает список из первых count фильмов по количеству лайков.
    Если значение параметра count не задано, верните первые 10.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopular(
            @RequestParam(defaultValue = "10") @Positive int count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        log.info("Получение популярных фильмов, количество: {}", count);
        return ResponseEntity.ok(filmService.getPopular(count, genreId, year));
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<Film>> getFilmsByDirectorSorted(
            @PathVariable int directorId,
            @RequestParam(name = "sortBy", defaultValue = "likes") String sortBy
    ) {
        // допустимые значения
        if (!sortBy.equals("likes") && !sortBy.equals("year")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(filmService.getByDirectorSorted(directorId, sortBy));
    }
}
