package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.InMemoryStorage;
import ru.yandex.practicum.filmorate.service.InMemoryStorageImpl;


import java.net.URI;
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
    public ResponseEntity<Film> addFilm(@RequestBody Film film) {
        Film addedFilm = filmStorage.create(film);
        URI location = URI.create("/films/" + addedFilm.getId());
        return ResponseEntity.created(location).body(addedFilm);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Film> updateFilm(@PathVariable int id, @RequestBody Film film) {
        return filmStorage.update(id, film)
                .map(updatedFilm -> ResponseEntity.ok(updatedFilm))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        return ResponseEntity.ok(filmStorage.findAll());
    }

}
