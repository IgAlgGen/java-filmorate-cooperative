SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
FROM films f
LEFT JOIN film_likes  fl ON fl.film_id = f.id
LEFT JOIN film_genres fg ON fg.film_id  = f.id
WHERE (:genreId IS NULL OR fg.genre_id = :genreId)
  AND (:year    IS NULL OR YEAR(f.release_date) = :year)
GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa
ORDER BY COUNT(fl.user_id) DESC, f.id ASC
LIMIT :limit