package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Identifiable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
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
        return item;
    }

    @Override
    public Optional<T> update(int id, T item) {
        if (!storage.containsKey(id)) {
            return Optional.empty();
        }
        item.setId(id);
        storage.put(id, item);
        return Optional.of(item);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<T> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }
}
