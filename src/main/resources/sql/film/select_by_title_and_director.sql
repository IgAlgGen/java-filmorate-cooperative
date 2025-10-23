SELECT f.id, f.name, f.release_date, f.description, f.duration, f.mpa
FROM films f
LEFT JOIN (
    SELECT fl.film_id, COUNT(fl.user_id) AS likes_cnt
    FROM film_likes fl
    GROUP BY fl.film_id
) lc ON lc.film_id = f.id
WHERE %s
ORDER BY COALESCE(lc.likes_cnt, 0) DESC, f.name ASC, f.id ASC