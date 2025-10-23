package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public Review createNewReview(@Valid @RequestBody Review review) {
        return reviewService.createNewReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReviewForId(@PathVariable Long id) {
        return reviewService.getReviewForId(id);
    }

    @GetMapping
    public List<Review> getReviewsForFilmId(@RequestParam(defaultValue = "0") @Positive int filmId,
                                            @RequestParam(defaultValue = "10") @Positive int count) {
        return reviewService.getReviewsForFilmId(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLikeReview(@PathVariable Long id, @PathVariable int userId) {
        reviewService.addLikeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislikeReview(@PathVariable Long id, @PathVariable int userId) {
        reviewService.addDislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeReview(@PathVariable Long id, @PathVariable int userId) {
        reviewService.deleteLikeReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislikeReview(@PathVariable Long id, @PathVariable int userId) {
        reviewService.deleteLikeReview(id, userId);
    }
}
