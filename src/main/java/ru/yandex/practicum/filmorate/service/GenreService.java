package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
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
        return genreStorage.findAll();
    }

    public Genre findById(int id) {
        log.debug("Поиск жанра по ID {}", id);
        return genreStorage.findById(id).orElse(null);
    }
}
