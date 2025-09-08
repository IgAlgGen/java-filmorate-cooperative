package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class User implements Identifiable {
    private int id;
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
    private String login;
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Email должен быть корректным")
    private String email;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Map<Integer, FriendshipStatus> friends = new HashMap<>();

    public void requestFriendship(int otherUserId) {
        friends.put(otherUserId, FriendshipStatus.UNCONFIRMED);
    }
    public void confirmFriendship(int otherUserId) {
        friends.put(otherUserId, FriendshipStatus.CONFIRMED);
    }
    public void removeFriendship(int otherUserId) {
        friends.remove(otherUserId);
    }
}
