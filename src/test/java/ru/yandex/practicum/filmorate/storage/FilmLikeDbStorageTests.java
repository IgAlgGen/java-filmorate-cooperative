package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.storage.film.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.filmLike.FilmLikeDbStorage;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmLikeDbStorage.class, FilmRowMapper.class})
@Sql(scripts = {"classpath:schema.sql", "classpath:testData.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class FilmLikeDbStorageTests {

    private final FilmLikeDbStorage filmLikeDbStorage;

    @Test
    public void testGetUsersLikesData() {
        Map<Integer, Set<Integer>> usersLikesData = filmLikeDbStorage.getUsersLikesData();
        assertThat(usersLikesData).isNotNull();
        assertEquals(2, usersLikesData.size());
        assertTrue(usersLikesData.containsKey(1));
        assertTrue(usersLikesData.containsKey(2));
        assertTrue(usersLikesData.get(1).contains(1));
        assertTrue(usersLikesData.get(1).contains(2));
        assertTrue(usersLikesData.get(2).contains(1));
        assertTrue(usersLikesData.get(2).contains(2));
    }
}
