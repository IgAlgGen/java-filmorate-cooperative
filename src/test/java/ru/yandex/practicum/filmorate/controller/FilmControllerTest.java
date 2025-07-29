package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.exeptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private final FilmController controller = new FilmController();

    @Test
    @DisplayName("Добавление валидного фильма возвращает статус 201 и фильм. Тест не вызывает исключений.")
    void addValidFilmReturnsCreatedAndFilm_thenNoException() {
        Film film = new Film(1, "Film", LocalDate.of(2000, 1, 1), "Description", 120);
        ResponseEntity<Film> response = controller.addFilm(film);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals(film.getName(), response.getBody().getName());
        assertDoesNotThrow(() -> controller.validateFilm(film));
    }

    @Test
    @DisplayName("Добавление фильма с пустым названием выбрасывает ValidationException")
    void addFilmWithEmptyNameThrowsValidationException() {
        Film film = new Film(1, " ", LocalDate.of(2000, 1, 1), "Description", 120);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    @DisplayName("Добавление фильма с длинным описанием выбрасывает ValidationException")
    void addFilmWithLongDescriptionThrowsValidationException() {
        String longDescription = "a".repeat(201);
        Film film = new Film(1, "Film", LocalDate.of(2000, 1, 1), longDescription, 120);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    @DisplayName("Добавление фильма с датой релиза до 28.12.1895 выбрасывает ValidationException")
    void addFilmWithTooEarlyReleaseDateThrowsValidationException() {
        Film film = new Film(1, "Film", LocalDate.of(1800, 1, 1), "Description", 120);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    @DisplayName("Добавление фильма с нулевой продолжительностью выбрасывает ValidationException")
    void addFilmWithZeroDurationThrowsValidationException() {
        Film film = new Film(1, "Film", LocalDate.of(2000, 1, 1), "Description", 0);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    @DisplayName("Обновление существующего фильма возвращает обновленный фильм")
    void updateExistingFilmReturnsUpdatedFilm() {
        Film film = new Film(1, "Film", LocalDate.of(2000, 1, 1), "Description", 120);
        controller.addFilm(film);
        Film updated = new Film(1, "Updated", LocalDate.of(2001, 2, 2), "New Description", 150);
        ResponseEntity<Film> response = controller.updateFilm(1, updated);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated", response.getBody().getName());
    }

    @Test
    @DisplayName("Обновление несуществующего фильма возвращает 404 Not Found")
    void updateNonExistingFilmReturnsNotFound() {
        Film updated = new Film(99, "Updated", LocalDate.of(2001, 2, 2), "New Desc", 150);
        ResponseEntity<Film> response = controller.updateFilm(99, updated);
        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    @DisplayName("Обновление фильма с некорректными данными выбрасывает ValidationException")
    void updateFilmWithInvalidDataThrowsValidationException() {
        Film film = new Film(1, "Film", LocalDate.of(2000, 1, 1), "Description", 120);
        controller.addFilm(film);
        Film invalidUpdate = new Film(1, "", LocalDate.of(2000, 1, 1), "New Description", 120);
        assertThrows(ValidationException.class, () -> controller.updateFilm(1, invalidUpdate));
    }

    @Test
    @DisplayName("Получение всех фильмов возвращает корректный список")
    void getAllFilmsReturnsCorrectList() {
        Film film1 = new Film(1, "Film1", LocalDate.of(2000, 1, 1), "Desc1", 100);
        Film film2 = new Film(2, "Film2", LocalDate.of(2001, 2, 2), "Desc2", 120);
        controller.addFilm(film1);
        controller.addFilm(film2);
        ResponseEntity<List<Film>> response = controller.getAllFilms();
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().size() >= 2);
    }
}
