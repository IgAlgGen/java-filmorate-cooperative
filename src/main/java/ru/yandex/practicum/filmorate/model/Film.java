package ru.yandex.practicum.filmorate.model;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;

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
    @NotBlank
    private LocalDate releaseDate;
    @Size(max = 200)
    private String description;
    @Positive
    private int duration;
}
