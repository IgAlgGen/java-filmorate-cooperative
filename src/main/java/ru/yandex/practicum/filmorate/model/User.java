package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class User implements Identifiable {
    private int id;
    @NotBlank
    private String login;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    @Past
    private LocalDate birthday;
}
