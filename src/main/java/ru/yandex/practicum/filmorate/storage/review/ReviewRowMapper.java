package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("id"));
        review.setContent(rs.getString("content"));
        review.setUserId(rs.getInt("user_id"));
        review.setFilmId(rs.getInt("film_id"));
        review.setIsPositive(rs.getBoolean("is_positive"));

        int positive = rs.getInt("positive_likes_count");
        int negative = rs.getInt("negative_likes_count");
        int useful = positive - negative;
        review.setUseful(useful);

        return review;
    }
}
