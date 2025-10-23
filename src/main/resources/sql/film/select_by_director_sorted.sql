SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, COALESCE(lc.cnt, 0) AS likes_count
FROM films f
JOIN film_directors fd ON fd.film_id = f.id
LEFT JOIN (
   SELECT film_id, COUNT(*) AS cnt
   FROM film_likes
   GROUP BY film_id
) lc ON lc.film_id = f.id
WHERE fd.director_id = :directorId
ORDER BY %s