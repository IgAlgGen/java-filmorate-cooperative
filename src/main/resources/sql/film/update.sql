UPDATE films
SET name = :name, description = :description, release_date = :releaseDate, duration = :duration, mpa = :mpa
WHERE id = :id;