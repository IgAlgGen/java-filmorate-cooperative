package ru.yandex.practicum.filmorate.controller;

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
        validateFilm(film);
        Film addedFilm = filmStorage.create(film);
        URI location = URI.create("/films/" + addedFilm.getId());
        return ResponseEntity.created(location).body(addedFilm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable int id, @Valid @RequestBody Film film) {
        validateFilm(film);
        return filmStorage.update(id, film)
                .map(updatedFilm -> ResponseEntity.ok(updatedFilm))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(filmStorage.findAll());
    }

    // валидация полей Film
    private void validateFilm(Film film) {
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
