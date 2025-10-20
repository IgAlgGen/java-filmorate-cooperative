package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        return directorStorage.update(director);
    }

    public Director getById(int id) {
       return directorStorage.getById(id).orElseThrow(() ->
               new NotFoundException(String.format("Режиссер с ID %d не найден", id)));
    }

    public void delete(int id) {
        directorStorage.deleteById(id);
    }

    public List<Director> getAll() {
        return directorStorage.getAll();
    }
}
