package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
@Sql(scripts = {"classpath:schema.sql", "classpath:testData.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class GenreDbStorageTest {
    private final GenreDbStorage genreStorage;

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = genreStorage.findById(1);
        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Comedy")
                );
    }

    @Test
    public void testFindAllGenres() {
        var genres = genreStorage.findAll();
        assertThat(genres).isNotNull();
        assertThat(genres.size()).isEqualTo(21);
    }


    @Test
    public void createGenreTest() {
        Genre newGenre = new Genre(22, "Sci-Fi");
        Genre createdGenre = genreStorage.create(newGenre);
        assertThat(createdGenre).hasFieldOrPropertyWithValue("id", 22)
                .hasFieldOrPropertyWithValue("name", "Sci-Fi");
    }

    @Test
    public void deleteGenreTest() {
        boolean deleted = genreStorage.deleteById(7);
        assertThat(deleted).isTrue();
        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> genreStorage.findById(7).orElseThrow(() ->
                        new NotFoundException("Жанр с ID 7 не найден")));
    }
}
