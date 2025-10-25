SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
FROM films AS f
JOIN film_likes AS fl ON f.id = fl.film_id
WHERE f.id IN (
    SELECT film_id FROM film_likes WHERE user_id = :userId
)
AND f.id IN (
    SELECT film_id FROM film_likes WHERE user_id = :friendId
)
GROUP BY f.id
ORDER BY COUNT(fl.user_id) DESC;