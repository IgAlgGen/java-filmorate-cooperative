package ru.yandex.practicum.filmorate.service.validators;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

/**
 * Сервис для валидации полей объекта User.
 */
@Service
public class UserValidator {
    /**
     * Валидация полей объекта User.<br>
     * <p>
     1. Проверяет, что email не пустой и содержит символ '@'.<br>
     2. Проверяет, что login не пустой и не содержит пробелы.<br>
     3. Если имя пустое, подставляет значение login.<br>
     4. Проверяет, что дата рождения не в будущем.<br>
     * <p>
     * @throws ValidationException если какое-либо из полей не соответствует условиям
     * @param user объект User для валидации
     */
    public void validateUser(User user) {
        // email
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email не может быть пустым и должен содержать символ '@'.");
        }
        // login
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы.");
        }
        // имя: если пустое, подставляем login
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        // день рождения
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
    }
}
