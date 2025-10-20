package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
@RequiredArgsConstructor
public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String name;

    public static MpaRating fromId(int id) {
        for (MpaRating rating : values()) {
            if (rating.id == id) {
                return rating;
            }
        }
        throw new NotFoundException("Неизвестный MPA id: " + id);
    }

    @JsonCreator
    public static MpaRating fromJson(JsonNode node) {
        if (node.isInt()) {
            return fromId(node.asInt());
        }
        if (node.isObject() && node.has("id")) {
            return fromId(node.get("id").asInt());
        }
        throw new IllegalArgumentException("неизвестный MPA: " + node);
    }
}
