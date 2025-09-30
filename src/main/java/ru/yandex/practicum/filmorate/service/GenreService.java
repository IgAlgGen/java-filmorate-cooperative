package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    public List<Genre> findAll() {
        log.debug("Поиск всех жанров");
        List<Genre> list = genreStorage.findAll();
        log.debug("Найдено {} жанров", list.size());
        return list;
    }

    public Genre findById(int id) {
        log.debug("Поиск жанра по ID {}", id);
        Genre genre = genreStorage.findById(id).orElse(null);
        if (genre != null) {
            log.debug("Найден жанр: id={}, name='{}'", genre.getId(), genre.getName());
        } else {
            log.debug("Жанр с ID {} не найден", id);
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        }
        return genre;
    }
}
