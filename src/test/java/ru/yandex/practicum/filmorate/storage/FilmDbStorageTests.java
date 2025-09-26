package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
@Sql(scripts = { "classpath:schema.sql", "classpath:testData.sql" },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public class FilmDbStorageTests {

    private final FilmDbStorage filmStorage;

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = filmStorage.findById(1);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", 1)
                                .hasFieldOrPropertyWithValue("name", "Film1")
                                .hasFieldOrPropertyWithValue("description", "Description1")
                                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2000, 1, 1))
                                .hasFieldOrPropertyWithValue("duration", 120)
                                .hasFieldOrPropertyWithValue("mpa", MpaRating.fromId(1))
                );
    }

    @Test
    public void createFilmTest() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        newFilm.setDuration(100);
        newFilm.setMpa(MpaRating.fromId(2));
        Film createdFilm = filmStorage.create(newFilm);
        assertThat(createdFilm).hasFieldOrPropertyWithValue("id", 4)
                .hasFieldOrPropertyWithValue("name", "New Film")
                .hasFieldOrPropertyWithValue("description", "New Description")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2020, 1, 1))
                .hasFieldOrPropertyWithValue("duration", 100)
                .hasFieldOrPropertyWithValue("mpa", MpaRating.fromId(2));
    }

    @Test
    public void updateFilmTest() {
        Film updatedFilm = new Film();
        updatedFilm.setId(2);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2021, 1, 1));
        updatedFilm.setDuration(150);
        updatedFilm.setMpa(MpaRating.fromId(3));
        Film resultFilm = filmStorage.update(updatedFilm);
        assertThat(resultFilm).hasFieldOrPropertyWithValue("id", 2)
                .hasFieldOrPropertyWithValue("name", "Updated Film")
                .hasFieldOrPropertyWithValue("description", "Updated Description")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(2021, 1, 1))
                .hasFieldOrPropertyWithValue("duration", 150)
                .hasFieldOrPropertyWithValue("mpa", MpaRating.fromId(3));
    }

    @Test
    public void testFindFilmByNonExistentId() {
        Optional<Film> filmOptional = filmStorage.findById(999);
        assertThat(filmOptional).isNotPresent();
    }

    @Test
    public void testFindAllFilms() {
        var films = filmStorage.findAll();
        assertThat(films).isNotNull();
        assertThat(films.size()).isEqualTo(3);
    }

    @Test
    public void deleteFilmTest() {
        filmStorage.deleteById(3);
        var films = filmStorage.findAll();
        Optional<Film> deletedFilm = filmStorage.findById(3);
        assertThat(deletedFilm).isNotPresent();
        assertThat(films).isNotNull();
        assertThat(films.size()).isEqualTo(2);
    }
}
