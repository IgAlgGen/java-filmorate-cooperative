package ru.yandex.practicum.filmorate.service.utils;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


/**
 * Класс описывает данные, необходимые для рекомендации фильмов.
 * Содержит информацию о пользователе, его лайках фильмов
 * и оценку схожести с другим пользователем (тем, для которого нужно сделать рекомендацию).
 * Оценка схожести определяется количеством фильмов, которым оба пользователя поставили лайк.
 */
@Getter
@Setter
public class FilmRecommendationData {
    private int userId;
    private Set<Integer> likedFilmIds;
    private int similarityScore;

    public FilmRecommendationData(int userId) {
        this.userId = userId;
        this.likedFilmIds = new HashSet<>();
    }

    public void addLikedFilmId(int likedFilmId) {
        likedFilmIds.add(likedFilmId);
    }

    public void computeSimilarityScore(Set<Integer> targetUserLikedFilmIds) {
        Set<Integer> likesIntersection = new HashSet<>(likedFilmIds);
        likesIntersection.retainAll(targetUserLikedFilmIds);
        similarityScore = likesIntersection.size();
    }
}

