package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Identifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class InMemoryStorageImpl<T extends Identifiable> implements InMemoryStorage<T> {
    private final Map<Integer, T> storage;
    private final AtomicInteger counter;

    public InMemoryStorageImpl() {
        this.storage = new HashMap<>();
        this.counter = new AtomicInteger(1); // Начинаем с 1, чтобы ID начинались с 1
    }

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
        item.setId(id); // Устанавливаем ID для обновляемого элемента
        storage.put(id, item);
        return Optional.of(item);// Возвращаем обновленный элемент
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values()); // Возвращаем список всех элементов
    }
}
