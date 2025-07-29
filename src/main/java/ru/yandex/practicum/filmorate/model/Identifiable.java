package ru.yandex.practicum.filmorate.model;

/**
 * Интерфейс, гарантирующий, что у любого объекта есть методы getId() и setId(int).
 */
public interface Identifiable {
    int getId();
    void setId(int id);
}
