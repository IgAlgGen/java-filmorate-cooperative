SELECT g.id, g.name
FROM genres g
JOIN film_genres fg ON fg.genre_id = g.id
WHERE fg.film_id = :filmId
ORDER BY g.id