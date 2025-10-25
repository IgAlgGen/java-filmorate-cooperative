UPDATE reviews
SET content = :content, is_positive = :isPositive
WHERE id = :reviewId;