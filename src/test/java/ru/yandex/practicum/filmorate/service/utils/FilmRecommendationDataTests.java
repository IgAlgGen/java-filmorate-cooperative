package ru.yandex.practicum.filmorate.service.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmRecommendationDataTests {
    FilmRecommendationData filmRecommendationData;

    @BeforeEach
    public void setUp() {
        filmRecommendationData = new FilmRecommendationData(1);
    }

    @Test
    public void testAddLikedFilmId() {
        filmRecommendationData.addLikedFilmId(1);
        filmRecommendationData.addLikedFilmId(2);

        assertEquals(2, filmRecommendationData.getLikedFilmIds().size());
        assertTrue(filmRecommendationData.getLikedFilmIds().contains(1));
        assertTrue(filmRecommendationData.getLikedFilmIds().contains(2));
    }

    @Test
    public void testComputeSimilarityScore() {
        // targetUserLikedFilmIds: {} (пусто)
        // filmRecommendationData.likedFilmIds: {} (пусто)
        Set<Integer> targetUserLikedFilmIds = new HashSet<>();
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(0, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {1}
        // filmRecommendationData.likedFilmIds: {} (пусто)
        targetUserLikedFilmIds.add(1);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(0, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {} (пусто)
        // filmRecommendationData.likedFilmIds: {1}
        targetUserLikedFilmIds.clear();
        filmRecommendationData.addLikedFilmId(1);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(0, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {1}
        // filmRecommendationData.likedFilmIds: {1}
        targetUserLikedFilmIds.clear();
        targetUserLikedFilmIds.add(1);
        filmRecommendationData.getLikedFilmIds().clear();
        filmRecommendationData.addLikedFilmId(1);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(1, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {1, 2}
        // filmRecommendationData.likedFilmIds: {1}
        targetUserLikedFilmIds.clear();
        targetUserLikedFilmIds.add(1);
        targetUserLikedFilmIds.add(2);
        filmRecommendationData.getLikedFilmIds().clear();
        filmRecommendationData.addLikedFilmId(1);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(1, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {1}
        // filmRecommendationData.likedFilmIds: {1, 2}
        targetUserLikedFilmIds.clear();
        targetUserLikedFilmIds.add(1);
        filmRecommendationData.getLikedFilmIds().clear();
        filmRecommendationData.addLikedFilmId(1);
        filmRecommendationData.addLikedFilmId(2);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(1, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {1, 2}
        // filmRecommendationData.likedFilmIds: {1, 2}
        targetUserLikedFilmIds.clear();
        targetUserLikedFilmIds.add(1);
        targetUserLikedFilmIds.add(2);
        filmRecommendationData.getLikedFilmIds().clear();
        filmRecommendationData.addLikedFilmId(1);
        filmRecommendationData.addLikedFilmId(2);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(2, filmRecommendationData.getSimilarityScore());

        // targetUserLikedFilmIds: {1, 2}
        // filmRecommendationData.likedFilmIds: {3, 4}
        targetUserLikedFilmIds.clear();
        targetUserLikedFilmIds.add(1);
        targetUserLikedFilmIds.add(2);
        filmRecommendationData.getLikedFilmIds().clear();
        filmRecommendationData.addLikedFilmId(3);
        filmRecommendationData.addLikedFilmId(4);
        filmRecommendationData.computeSimilarityScore(targetUserLikedFilmIds);
        assertEquals(0, filmRecommendationData.getSimilarityScore());
    }
}
