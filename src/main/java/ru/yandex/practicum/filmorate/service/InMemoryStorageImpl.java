package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Identifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryStorageImpl<T extends Identifiable> implements InMemoryStorage<T> {
    private final Map<Integer, T> storage = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public T create(T item) {
        int id = counter.getAndIncrement();
        item.setId(id);
        storage.put(id, item);
        return item; // Возвращаем созданный элемент
    }

    @Override
    public Optional<T> update(int id, T item) {
        if (!storage.containsKey(id)) {
            return Optional.empty(); // Если элемент не найден, возвращаем пустой Optional
        }
        storage.put(id, item);
        return Optional.of(item);// Возвращаем обновленный элемент
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values()); // Возвращаем список всех элементов
    }
}
