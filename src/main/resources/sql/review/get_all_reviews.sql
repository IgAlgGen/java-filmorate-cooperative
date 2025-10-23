SELECT r.id, r.content, r.user_id, r.film_id, r.is_positive,
COUNT(CASE WHEN l.is_like = TRUE  THEN 1 END) AS positive_likes_count,
COUNT(CASE WHEN l.is_like = FALSE THEN 1 END) AS negative_likes_count,
(COUNT(CASE WHEN l.is_like = TRUE  THEN 1 END) -
COUNT(CASE WHEN l.is_like = FALSE THEN 1 END)) AS rating_score
FROM reviews r
LEFT JOIN reviews_likes l ON r.id = l.review_id
GROUP BY r.id, r.content, r.user_id, r.film_id
ORDER BY rating_score DESC, r.id ASC
LIMIT :count;