package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Director implements Identifiable {
    private int id;
    @NotNull (message = "Имя режиссера не может быть null.")
    @NotEmpty (message = "Имя режиссера не может быть пустым.")
    @NotBlank (message = "Имя режиссера не может быть null и должен содержать хотя бы один символ, отличный от пробелов.")
    private String name;
}
