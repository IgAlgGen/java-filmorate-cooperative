package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
