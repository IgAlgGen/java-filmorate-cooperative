package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Identifiable;

import java.util.List;
import java.util.Optional;

public interface InMemoryStorage<T extends Identifiable> {
    /**
     * Создает новый элемент в хранилище.
     */
    T create(T item);

    /**
     * Обновляет существующий элемент в хранилище.
     */
    Optional<T> update(int id, T item);

    /**
     * Находит все элементы в хранилище.
     */
    List<T> findAll();

    /**
     * Находит элемент по идентификатору.
     */
    Optional<T> findById(int id);
}
