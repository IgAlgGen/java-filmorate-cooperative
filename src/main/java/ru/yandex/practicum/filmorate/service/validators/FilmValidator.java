package ru.yandex.practicum.filmorate.service.validators;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

/**
 * Сервис для валидации полей объекта Film.
 */
@Service
public class FilmValidator {
    /**
     * Валидация полей объекта Film.<br>
     * <p>
     * 1. Проверяет, что название фильма не пустое.<br>
     * 2. Проверяет, что описание не превышает 200 символов.<br>
     * 3. Проверяет, что дата релиза не раньше 28 декабря 1895 года.<br>
     * 4. Проверяет, что продолжительность фильма положительна.<br>
     * <p>
     *
     * @throws ValidationException если какое-либо из полей не соответствует условиям
     * @param film объект Film для валидации
     */
    public static void validateFilm(Film film) {
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
