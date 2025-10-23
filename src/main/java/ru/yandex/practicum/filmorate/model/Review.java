package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {

    private Long reviewId;
    @NotNull(message = "Отзыв фильма не может быть пустым.")
    @NotEmpty
    @NotBlank(message = "Отзыв фильма не может быть пустым.")
    private String content;
    @NotNull(message = "Статус фильма не может быть пустым.")
    @JsonProperty("isPositive")
    private Boolean isPositive;
    @NotNull(message = "id пользователя не может быть пустым.")
    private Integer userId;
    @NotNull(message = "id фильма не может быть пустым.")
    private Integer filmId;
    private int useful;
}
