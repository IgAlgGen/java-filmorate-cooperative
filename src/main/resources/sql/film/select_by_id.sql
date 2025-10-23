SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa
FROM films f
WHERE f.id = :id;