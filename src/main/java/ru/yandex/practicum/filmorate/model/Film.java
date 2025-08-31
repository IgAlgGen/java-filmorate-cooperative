package ru.yandex.practicum.filmorate.model;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import ru.yandex.practicum.filmorate.service.validators.constraints.ReleaseDateMin;

/**
 * Film.
 */
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Film implements Identifiable {
    private int id;
    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;
    @NotNull
    @ReleaseDateMin("1895-12-28")
    private LocalDate releaseDate;
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов.")
    private String description;
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private int duration;

    /**
     * Множество идентификаторов пользователей, поставивших лайк.
     */
    private Set<Integer> likes = new HashSet<>();

    public void addLike(int userId) {
        likes.add(userId);
    }

    public void removeLike(int userId) {
        likes.remove(userId);
    }

    public int likeCount() {
        return likes.size();
    }
}
