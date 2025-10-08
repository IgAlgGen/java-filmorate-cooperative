package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserRowMapper;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
@Sql(scripts = { "classpath:schema.sql", "classpath:testData.sql" },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class UserDbStorageTests {
    private final UserDbStorage userStorage;

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = userStorage.getById(1);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("email", "test1@example.com")
                                .hasFieldOrPropertyWithValue("login", "test1")
                                .hasFieldOrPropertyWithValue("name", "Test1")
                                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 1, 1))
                );
    }

    @Test
    public void testCreateUser() {
        User newUser = new User(0, "newuser", "New User", "newUSer@example.com", LocalDate.of(1995, 5, 15), new HashSet<>());
        User createdUser = userStorage.create(newUser);
        User retrievedUser = userStorage.getById(createdUser.getId()).orElseThrow();
        assertThat(retrievedUser).isEqualTo(createdUser);
    }

    @Test
    public void testUpdateUser() {
        User existingUser = userStorage.getById(2).orElseThrow();
        existingUser.setEmail("newmail@examplecom");
        existingUser.setLogin("newlogin");
        existingUser.setName("New Name");
        existingUser.setBirthday(LocalDate.of(1992, 2, 2));
        userStorage.update(existingUser);
        User updatedUser = userStorage.getById(2).orElseThrow();
        assertThat(updatedUser).isEqualTo(existingUser);
    }

    @Test
    public void testDeleteUser() {
        userStorage.deleteById(3);
        Optional<User> deletedUser = userStorage.getById(3);
        assertThat(deletedUser).isNotPresent();
    }

    @Test
    public void testGetAllUsers() {
        var users = userStorage.getAll();
        assertThat(users.size()).isEqualTo(3);
    }

    @Test
    public void testFindUserByIdNotFound() {
        Optional<User> userOptional = userStorage.getById(999);
        assertThat(userOptional).isNotPresent();
    }

}


