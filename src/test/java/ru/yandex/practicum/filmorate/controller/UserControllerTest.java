package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {
    private  final UserController controller = new UserController();

    @Test
    @DisplayName("Добавление пользователя с валидными данными возвращает статус 201 и пользователя. Тест не вызывает исключений.")
    void addUserWithValidDataReturnsCreatedAndUser() {
        User user = new User(1, "userlogin", "User Name", "user@example.com", LocalDate.of(1990, 1, 1));
        ResponseEntity<User> response = controller.addUser(user);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(user.getEmail(), response.getBody().getEmail());
        assertDoesNotThrow(() -> controller.validateUser(user));
    }

    @Test
    @DisplayName("Добавление пользователя с пустым email выбрасывает ValidationException")
    void addUserWithEmptyEmailThrowsValidationException() {
        User user = new User(1, "userlogin", "User Name", "", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.addUser(user));
        assertTrue(ex.getMessage().contains("Email не может быть пустым"));
    }

    @Test
    @DisplayName("Добавление пользователя с email без символа '@' выбрасывает ValidationException")
    void addUserWithInvalidEmailThrowsValidationException() {
        User user = new User(1, "userlogin", "User Name", "user?example.com", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.addUser(user));
        assertTrue(ex.getMessage().contains("должен содержать символ '@'"));
    }

    @Test
    @DisplayName("Добавление пользователя с пустым логином выбрасывает ValidationException")
    void addUserWithEmptyLoginThrowsValidationException() {
        User user = new User(1, "", "User Name", "user@example.com", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.addUser(user));
        assertTrue(ex.getMessage().contains("Логин не может быть пустым"));
    }

    @Test
    @DisplayName("Добавление пользователя с логином, содержащим пробелы, выбрасывает ValidationException")
    void addUserWithLoginContainingSpacesThrowsValidationException() {
        User user = new User(1, "user login", "User Name", "user@example.com", LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.addUser(user));
        assertTrue(ex.getMessage().contains("не должен содержать пробелы"));
    }

    @Test
    @DisplayName("Добавление пользователя с пустым именем подставляет логин в качестве имени. Тест не вызывает исключений.")
    void addUserWithEmptyNameUsesLoginAsName_thenNoException() {
        User user = new User(1, "userlogin", "", "user@example.com", LocalDate.of(1990, 1, 1));
        ResponseEntity<User> response = controller.addUser(user);
        assertEquals("userlogin", response.getBody().getName());
        assertDoesNotThrow(() -> controller.validateUser(user));
    }

    @Test
    @DisplayName("Добавление пользователя с датой рождения в будущем выбрасывает ValidationException")
    void addUserWithFutureBirthdayThrowsValidationException() {
        User user = new User(1, "userlogin", "User Name", "user@example.com", LocalDate.now().plusDays(1));
        ValidationException ex = assertThrows(ValidationException.class, () -> controller.addUser(user));
        assertTrue(ex.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("Обновление существующего пользователя возвращает обновленного пользователя")
    void updateExistingUserReturnsUpdatedUser() {
        User user = new User(1, "userlogin", "User Name", "user@example.com", LocalDate.of(1990, 1, 1));
        controller.addUser(user);
        User updatedUser = new User(1, "userloginUpdated", "User Name Updated", "userUpdated@example.com", LocalDate.of(1990, 1, 1));
        ResponseEntity<User> response = controller.updateUser(1, updatedUser);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("userUpdated@example.com", response.getBody().getEmail());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя возвращает 404 Not Found")
    void updateNonExistingUserReturnsNotFound() {
        User updatedUser = new User(99, "userlogin", "User Name", "user@example.com", LocalDate.of(1990, 1, 1));
        ResponseEntity<User> response = controller.updateUser(99, updatedUser);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Получение всех пользователей возвращает корректный список")
    void getAllUsersReturnsCorrectList() {
        User user1 = new User(1, "userlogin1", "User Name1", "user1@example.com", LocalDate.of(1990, 1, 1));
        User user2 = new User(1, "userlogin2", "User Name2", "user2@example.com", LocalDate.of(1990, 1, 1));
        controller.addUser(user1);
        controller.addUser(user2);
        ResponseEntity<List<User>> response = controller.getAllUsers();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().size() >= 2);
    }
}
