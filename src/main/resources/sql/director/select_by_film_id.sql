SELECT d.id, d.name
FROM directors d
JOIN film_directors fd ON fd.director_id = d.id
WHERE fd.film_id = :filmId
ORDER BY d.id;