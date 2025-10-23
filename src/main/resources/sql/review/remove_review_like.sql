DELETE FROM reviews_likes
WHERE review_id = :reviewId AND user_id = :userId;