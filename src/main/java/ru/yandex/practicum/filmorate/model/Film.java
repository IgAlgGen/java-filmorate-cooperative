package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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
    @NotBlank
    private String name;
    @NotNull
    private LocalDate releaseDate;
    @Size(max = 200)
    private String description;
    @Positive
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
