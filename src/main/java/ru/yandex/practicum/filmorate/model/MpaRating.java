package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String title;

    public static MpaRating fromId(int id) {
        for (MpaRating rating : values()) {
            if (rating.id == id) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Неизвестный MPA id: " + id);
    }
}
