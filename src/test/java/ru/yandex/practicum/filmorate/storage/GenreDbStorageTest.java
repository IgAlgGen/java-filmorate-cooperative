package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class})
@Sql(scripts = { "classpath:schema.sql", "classpath:testData.sql" },
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
        assertThat(genres.size()).isEqualTo(7);
        assertThat(genres.containsAll(List.of(
                new Genre(1, "Comedy"),
                new Genre(2, "Drama"),
                new Genre(3, "Animation"),
                new Genre(4, "Thriller"),
                new Genre(5, "Documentary"),
                new Genre(6, "Action"),
                new Genre(7, "Horror")))).isTrue();
    }

    @Test
    public void testFindGenreByInvalidId() {
        Optional<Genre> genreOptional = genreStorage.findById(999);
        assertThat(genreOptional).isNotPresent();
    }

    @Test
    public void createGenreTest() {
        Genre newGenre = new Genre(8,"Sci-Fi");
        Genre createdGenre = genreStorage.create(newGenre);
        assertThat(createdGenre).hasFieldOrPropertyWithValue("id", 8)
                .hasFieldOrPropertyWithValue("name", "Sci-Fi");
    }

    @Test
    public void deleteGenreTest() {
        boolean deleted = genreStorage.deleteById(7);
        assertThat(deleted).isTrue();
        Optional<Genre> genreOptional = genreStorage.findById(7);
        assertThat(genreOptional).isNotPresent();
    }
}
