package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Director implements Identifiable {
    private int id;
    @NotNull
    @NotBlank (message = "Имя режиссера не может быть пустым.")
    private String name;
}
